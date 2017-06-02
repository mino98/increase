package it.minux.increase.layers.coverage;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.terrain.CompoundElevationModel;
import it.minux.increase.data.Panel;
import it.minux.increase.ui.Config;
import it.minux.increase.visibility.ElevationHelper;

import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Represent area coverage as byte array where 
 * 	0 - not covered
 *  != 0 - covered
 *  
 *  *--------------------------->x (lon)
 *  |
 *  |
 *  |
 *  |y (lat)
 *  
 *  array index = x + y * width 
 * 
 * @author brujito
 *
 */
public class CoverageMap {

	private static final Log LOG = LogFactory.getLog(CoverageMap.class);
	
	private final Globe globe;
	private final Sector sector;

	private final int width;
	private final int height;
	
	private final double iLatres; // 1 / latres
	private final double iLonres; // 1 / lonres
	private final TowersTree tree;
	
	private double[] map = null;
	
	public CoverageMap(Globe globe, TowersTree tree, Sector sector, int latres, int lonres) {
		this.globe = globe;
		this.tree = tree;
		this.sector = sector;
		this.height = (int)Math.round(sector.getDeltaLat().degrees * latres);
		this.width = (int)Math.round(sector.getDeltaLon().degrees * lonres);
		this.iLatres = 1.0 / latres;
		this.iLonres = 1.0 / lonres;
	}
	
	public Sector getSector() {
		return sector;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
//	/**
//	 * Update coverage map with portion of towers
//	 * @param towers
//	 */
//	public void update(List<Tower> towers) {
//		tree.insertTowers(towers, globe);
//	}
	
	private void computeCoverage(int y1, int y2) {
		for (int y = y1; y < y2; ++y) {
			if (((y - y1) % 100) == 0) {
				LOG.info((y - y1) + " of " + (y2 - y1) + " lines processed");
			}
			
 			ElevationHelper.INSTANCE.ensureElevationLoaded(computeScanlineBounds(y), globe);
			for (int x = 0; x < width; ++x) {
				map[x + y * width] = computeQuality(x, y);
			}
		}
	}
	
//	private IAdvElevationModel advElevationModel = null;
//	
//	private IAdvElevationModel getAdvWMSElevationModel() {
//		if (advElevationModel == null) {
//			ElevationModel em = globe.getElevationModel();
//			if (em instanceof CompoundElevationModel) {
//				for (ElevationModel emi : ((CompoundElevationModel)em).getElevationModels()) {
//					if (emi instanceof IAdvElevationModel) {
//						advElevationModel = (IAdvElevationModel)emi;
//						break;
//					}
//				}
//			} else if (em instanceof IAdvElevationModel) {
//				advElevationModel = (IAdvElevationModel)em;
//			}
//		}
//		
//		return advElevationModel;
//	}

//	  private int requiredLevel = -1;
//	  
//	  /**
//	   * Which level of DTM is required for compution
//	   * @return
//	   */
//	  private int getRequiredLevel() {
//	  	if (requiredLevel < 0) {
//	  		requiredLevel = Config.INSTANCE.getIntValue(Config.COVERAGE_DTM_LEVEL, 9);
//	  	}
//	  	
//	  	return requiredLevel;
//	  }
	
//	/**
//	 * Ensure elevation model tiles are in memory before computing scanline
//	 * @param y
//	 */
//	private void ensureElevationLoaded(int y) {
//		IAdvElevationModel em = getAdvWMSElevationModel();
//		if (em == null) {
//			LOG.error("Failed to find AdvWMSElevationModel, can't preload elevation tiles");
//			return ;
//		}
//		Sector s = computeScanlineBounds(y);
//		LOG.debug("preload: " + s);
//		em.loadMaxResolution(s, ElevationHelper.INSTANCE.getRequiredLevel());
//		
////		LOG.debug("em: " + em);
////		if (!(em instanceof ))
//	}

	private Sector computeScanlineBounds(int y) {
		LatLon l1 = toLocation(0, y);
		LatLon l2 = toLocation(width - 1, y);
		LatLon l0 = LatLon.fromDegrees(l1.latitude.degrees, (l1.longitude.degrees + l2.longitude.degrees)/2);
		
		Sector s1 = Sector.boundingSector(globe, l0, 30 * 1000); // 30 km up and down
		Sector s2 = Sector.boundingSector(l1, l2);
//		s1 = s1.union(l1.latitude, l1.longitude);
//		s1 = s1.union(l2.latitude, l2.longitude);
		Sector b = s1.union(s2);
		return b;
	}

//	
//	private class ChunkProcessor 
//		implements Runnable {
//		
//		private final int y1;
//		private final int y2;
//		
//		public ChunkProcessor(int y1, int y2) {
//			this.y1 = y1;
//			this.y2 = y2;
//		}
//
//		@Override
//		public void run() {
//			computeCoverage(y1, y2);
//		}
//	}
	
	public void computeCoverage() {
		ensureMapCreated();
		computeCoverage(0, height);
		
		
//		new ChunkProcessor(0, height).run();
//		Thread t1 = new Thread(new ChunkProcessor(0, height / 2));
//		Thread t2 = new Thread(new ChunkProcessor(height / 2, height));
//		
//		t1.start();
//		t2.start();
//		
//		try {
//			t1.join();
//		} catch (Exception e) {
//			LOG.error(e);
//		}
//		
//		try { 
//			t2.join();
//		} catch (Exception e) {
//			LOG.error(e);
//		}
	}
	
	private double computeQuality(int x, int y) {
		LatLon loc = toLocation(x, y);
		List<Panel> possiblePanels = tree.query(loc);
		if (possiblePanels.isEmpty()) {
			return 0;
		}
		
		double elevation = globe.getElevation(loc.latitude, loc.longitude);
		Vec4 point = globe.computePointFromPosition(loc, elevation + 2.0);
		
		Collections.sort(possiblePanels, new DistanceToPointComaparator(loc));
		
		double maxQuality = 0;
		for (Panel panel : possiblePanels) {
			double quality = panel.qualityAt(loc, point, globe);
			if (quality > 0) {
				return quality;
			}
			
			// don't need if we sort em with distance
//			if (quality > maxQuality) {
//				maxQuality = quality;
//			}
		}
		
		return maxQuality;
	}

	public LatLon toLocation(int x, int y) {
		if (x < 0 || x >= width) {
			throw new IllegalArgumentException("x = " + x);
		}
		
		if (y < 0 || y >= height) {
			throw new IllegalArgumentException("y = " + y);
		}
		
		Angle lon =  Angle.fromDegrees(x * iLonres).add(sector.getMinLongitude());
		Angle lat =  sector.getMaxLatitude().subtract(Angle.fromDegrees(y * iLatres));
		
		return new LatLon(lat, lon);
	}

	public boolean isComputed() {
		return map != null;
	}
	
	public double[] getMap() {
		return map;
	}

	private void ensureMapCreated() {
		if (map == null) {
			map = new double[width * height];
		}
	}
}
