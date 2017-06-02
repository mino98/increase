package it.minux.increase.layers;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.examples.analytics.AnalyticSurface;
import gov.nasa.worldwind.examples.analytics.AnalyticSurfaceAttributes;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.BufferFactory;
import gov.nasa.worldwind.util.BufferWrapper;
import it.minux.increase.data.PointEvent;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Render list of PointEvents as heatmap
 * @author brujito
 *
 * @param <T>
 */
public class Heatmap<T extends PointEvent> 
	extends AnalyticSurface
{
	private static final Log LOG = LogFactory.getLog(Heatmap.class);
	
	private final int latres;
	private final int lonres;
	
    protected static final double HEATMAP_MIN_VALUE = 0;
    protected static final double HEATMAP_MAX_VALUE = 1000;
    protected static final double HEATMAP_HUE_BLUE = 240d / 360d;
    protected static final double HEATMAP_HUE_RED = 0d / 360d;
    protected static final double HEATMAP_SMOOTHING = 0.4d;
    
    private double[] values = null;
    private double[] normValues = null;
	
	public Heatmap(Sector sector, int latres, int lonres) {
		this.latres = latres;
		this.lonres = lonres;
		
		setSector(sector);
		
		int height = (int)Math.round(sector.getDeltaLat().degrees * latres);
		int width = (int)Math.round(sector.getDeltaLon().degrees * lonres);
		setDimensions(width, height);
		setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
		
//		values = createData(events);
	}
	
	public void initData(List<T> events) {
		createData(events);
	}

	public double[] getRawData() {
		return values;
	}
	
	public double[] getNormData() {
		return normValues;
	}
	
	private void createData(List<T> events) {
		// Define a 2D array, and for each cell, count how many coverage requests fall in it:
        int numValues = width * height;
        this.values = new double[numValues];
        this.normValues = new double[numValues];
        
        for (T event : events) {
        	LatLon loc = event.getLocation();
        	if (sector.contains(loc)) {
        		int x = (int) ((loc.getLongitude().degrees - sector.getMinLongitude().degrees) * lonres);
        		int y = (int) ((sector.getMaxLatitude().degrees - loc.getLatitude().degrees) * latres);
        		int index = x + y * width;
        		
        		if (index >= 0 && index < numValues) {
        			values[index] ++;
        			normValues[index] ++;
        		} else {
        			LOG.error("Invalid index: " + index + ", " + x + ", " + y);
        		}        		
        	} else {
        		LOG.warn("Event out of sector: " + loc);
        	}
        }
        
        preprocessValues(width, height, normValues);
        
        // Smooth a bit the heatmap:
        smoothValues(width, height, normValues);
        
        // Scales the values between the min and the max. TODO is this needed?
        scaleValues(normValues, numValues, HEATMAP_MIN_VALUE, HEATMAP_MAX_VALUE);

        createSurfacePoints(this, numValues, normValues);        
		
		
//		return values;
	}

	/**
	 * Template method for some data preprocessing
	 * @param width
	 * @param height
	 * @param values
	 */
	protected void preprocessValues(int width, int height, double[] values) {
		
	}

	static void createSurfacePoints(AnalyticSurface surface, int numValues, double[] values) {
		// Create a buffer from the array:
        BufferWrapper buffer = new BufferFactory.DoubleBufferFactory().newBuffer(numValues);
        buffer.putDouble(0, values, 0, numValues);
							
		ArrayList<AnalyticSurface.GridPointAttributes> attributesList = new ArrayList<AnalyticSurface.GridPointAttributes>();
		
		// Color the map:
		long length = buffer.length();
		for (int i = 0; i < length; i++) {
			attributesList.add(
				AnalyticSurface.createColorGradientAttributes(buffer.getDouble(i), HEATMAP_MIN_VALUE, HEATMAP_MAX_VALUE, HEATMAP_HUE_BLUE, HEATMAP_HUE_RED));
		}    
		surface.setValues(attributesList);
		
		// Set extra attributes for the Analytic surface:
		AnalyticSurfaceAttributes attr = new AnalyticSurfaceAttributes();
		attr.setDrawShadow(false);
		attr.setInteriorOpacity(0.6);
		attr.setDrawOutline(false);
		surface.setSurfaceAttributes(attr);
	}

    /**
     * Linearly scale the values of the given array between minValue and maxValue
     * @param values
     * @param count
     * @param minValue
     * @param maxValue
     */
    static void scaleValues(double[] values, int count, double minValue, double maxValue)
    {
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for (int i = 0; i < count; i++)
        {
            if (min > values[i])
                min = values[i];
            if (max < values[i])
                max = values[i];
        }

        for (int i = 0; i < count; i++) {
            values[i] = (values[i] - min) / (max - min);
            values[i] = minValue + values[i] * (maxValue - minValue);
        }
    }
    
    /**
     * Smooth the values based on a 0-1 'smoothness' parameter.
     * @param width
     * @param height
     * @param values
     */
    static void smoothValues(int width, int height, double[] values) {
        // top to bottom
        for (int x = 0; x < width; x++)
        {
            smoothBand(values, x, width, height);
        }

        // bottom to top
        int lastRowOffset = (height - 1) * width;
        for (int x = 0; x < width; x++)
        {
            smoothBand(values, x + lastRowOffset, -width, height);
        }

        // left to right
        for (int y = 0; y < height; y++)
        {
            smoothBand(values, y * width, 1, width);
        }

        // right to left
        int lastColOffset = width - 1;
        for (int y = 0; y < height; y++)
        {
            smoothBand(values, lastColOffset + y * width, -1, width);
        }
    }
    
    /**
     * Smooth a single band of data (?)
     * @param values
     * @param start
     * @param stride
     * @param count
     */
    static void smoothBand(double[] values, int start, int stride, int count) {
        double prevValue = values[start];
        int j = start + stride;

        for (int i = 0; i < count - 1; i++)
        {
            values[j] = HEATMAP_SMOOTHING * prevValue + (1 - HEATMAP_SMOOTHING) * values[j];
            prevValue = values[j];
            j += stride;
        }
    }
	
	@Override
	public String toString() {
		int[] dim = getDimensions();
		return "Heatmap(sector=" + sector + ", w=" + dim[0] + ", h=" + dim[1] + ")"; 
	}
	
}
