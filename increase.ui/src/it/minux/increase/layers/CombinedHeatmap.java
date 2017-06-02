package it.minux.increase.layers;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.examples.analytics.AnalyticSurface;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;

public class CombinedHeatmap 
	extends AnalyticSurface
	implements Iterable<HeatmapPoint>
{
	
	private static final Log LOG = LogFactory.getLog(CombinedHeatmap.class);
	
	private final double[] coeffs;
	private final Heatmap<?>[] maps;
	
	private final double lonres;
	private final double latres;
	
	private final double[] values;
	
	public CombinedHeatmap(double[] coeffs, Heatmap<?>... maps) {
		this.coeffs = coeffs;
		this.maps = maps;
		
		if (coeffs.length != maps.length) {
			throw new IllegalArgumentException("coefficients");
		}
		
		// TODO validate size compatibility
		setSector(maps[0].getSector());
		int[] dim = maps[0].getDimensions();
		setDimensions(dim[0], dim[1]);
		setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
		
		lonres = width / sector.getDeltaLon().degrees;
		latres = height / sector.getDeltaLat().degrees;
		
		values = createData(coeffs, maps);
	}

	private double[] createData(double[] coeffs, Heatmap<?>[] maps) {
		// Define a 2D array, and for each cell, count how many coverage requests fall in it:
        int numValues = width * height;
        double[] values = new double[numValues];
		
		for (int i = 0; i < numValues; ++i) {
			values[i] = 0;
			for (int j = 0; j < maps.length; ++j) {
				values[i] += (coeffs[j]) * (maps[j].getNormData()[i]); 
			}
		}
		
        // Smooth a bit the heatmap:
        Heatmap.smoothValues(width, height, values);
        
        // Scales the values between the min and the max. TODO is this needed?
        Heatmap.scaleValues(values, numValues, Heatmap.HEATMAP_MIN_VALUE, Heatmap.HEATMAP_MAX_VALUE);

        Heatmap.createSurfacePoints(this, numValues, values); 
		
		
		return values;
	}
	
	public HeatmapPoint getHeatmapPoint(int index) {
		try {
			double value = values[index];
			LatLon loc = indexToLocation(index);
			HeatmapPoint point = new HeatmapPoint(index, loc, value);
			return point;
		} catch (ArrayIndexOutOfBoundsException e) {
			LOG.error("Invalid heatmap index: " + index);
			return null;
		}
	}
	
	public Area getArea(Sector aSector) {
		aSector = this.sector.intersection(aSector);
//		Angle lon = Angle.fromDegrees((x / lonres) + sector.getMinLongitude().degrees);
//		Angle lat = Angle.fromDegrees( - (y / latres) + sector.getMaxLatitude().degrees);
		
		if (aSector == null) {
			return null;
		}
		
		int minX = (int)Math.floor( (aSector.getMinLongitude().degrees - sector.getMinLongitude().degrees) * lonres); 
		int maxX = (int)Math.ceil( (aSector.getMaxLongitude().degrees - sector.getMinLongitude().degrees) * lonres); 
		
		if (maxX >= width) {
			maxX = width - 1;
		}
		
		int minY = (int)Math.floor( (sector.getMaxLatitude().degrees - aSector.getMaxLatitude().degrees) * latres); 
		int maxY = (int)Math.ceil( (sector.getMaxLatitude().degrees - aSector.getMinLatitude().degrees) * latres); 
		
		if (maxY >= height) {
			maxY = height - 1;
		}
		
		
		return new Area(minX, maxX, minY, maxY);
	}

	@Override
	public Iterator<HeatmapPoint> iterator() {
		return new PointsIterator();
	}
	
	private class PointsIterator 
		implements Iterator<HeatmapPoint>
	{
		private int nextId = 0;

		@Override
		public boolean hasNext() {
			return nextId < values.length;
		}

		@Override
		public HeatmapPoint next() {
			double value = values[nextId];
			LatLon loc = indexToLocation(nextId);
			HeatmapPoint point = new HeatmapPoint(nextId, loc, value);
			nextId ++;
			return point;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("remove()");
		}
	}
	
	private class AreaPointsIterator 
		implements Iterator<HeatmapPoint>
	{
		private final Area area;
		private int x;
		private int y;
		private int nextId;

		private AreaPointsIterator(Area area) {
			this.area = area;
			this.x = area.minX;
			this.y = area.minY;
			this.nextId = indexFromXY(x, y);
//			this.nextId = area.minX + area.minY * width;
		}

		@Override
		public boolean hasNext() {
			return nextId >= 0;
		}
	
		@Override
		public HeatmapPoint next() {
			double value = values[nextId];
			LatLon loc = indexToLocation(nextId);
			HeatmapPoint point = new HeatmapPoint(nextId, loc, value);
			updateNextId();
			return point;
		}
	
		private void updateNextId() {
			if (nextId < 0) {
				throw new IllegalStateException("next() called when no more elements in iterator");
			}
			
//			int x = nextId % width;
//			int y = nextId / width;
			
			x++;
			if (x > area.maxX) {
				// next line
				x = area.minX;
				y++;	
			}
			
			if (y > area.maxY) {
				nextId = -1; // no more elements
			} else {
				nextId = x + y * width;
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("remove()");
		}
	}
	
	/**
	 * Rectangle part of heatmap
	 * @author brujito
	 *
	 */
	public class Area 
		implements Iterable<HeatmapPoint>
	{
		private final int minX;
		private final int maxX; 
		private final int minY;
		private final int maxY;
		
		
		private Area(int minX, int maxX, int minY, int maxY) {
			if (!((minX >= 0) && (minX <= maxX) && (maxX < width))) {
				throw new IllegalArgumentException("min/max x: " + minX + "/" + maxX);
			}
			
			if (!((minY >= 0) && (minY <= maxY) && (maxY < height))) {
				throw new IllegalArgumentException("min/max y: " + minY + "/" + maxY);
			}
			
			this.minX = minX;
			this.maxX = maxX;
			this.minY = minY;
			this.maxY = maxY;
		}

		@Override
		public Iterator<HeatmapPoint> iterator() {
			return new AreaPointsIterator(this);
		}
	}
	
	private LatLon indexToLocation(int index) {
		int x = index % width;
		int y = index / width;
		
		Angle lon = Angle.fromDegrees((x / lonres) + sector.getMinLongitude().degrees);
		Angle lat = Angle.fromDegrees( - (y / latres) + sector.getMaxLatitude().degrees);
		
		return new LatLon(lat, lon);
	}
	
	private int indexFromXY(int x, int y) {
		return x + width * y;
	}
}
