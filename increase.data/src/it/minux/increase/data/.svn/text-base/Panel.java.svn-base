package it.minux.increase.data;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.RayCastingSupport;
import it.minux.increase.geom.AngularSector;
import it.minux.increase.xml.PanelType;

public class Panel {

	private final Tower tower;
	private final double height;
	private final Angle minTilt;
	private final Angle maxTilt;
//	private final Angle minAzimuth;
//	private final Angle maxAzimuth;
	
	private final AngularSector sector;
	private final double maxDistance; // in meters
	private final double maxDistanceSqared; // 
	
	private volatile Vec4 center = null; // coordinates of panel mount point in meters (WGS)
	
	private Panel(Tower tower, double height, Angle minTilt, Angle maxTilt, AngularSector sector, double maxDistance) {
		this.tower = tower;
		this.height = height;
		this.minTilt = minTilt;
		this.maxTilt = maxTilt;
//		this.sector = new AngularSector(minAzimuth, maxAzimuth);
		this.sector = sector;
		this.maxDistance = maxDistance;
		this.maxDistanceSqared = this.maxDistance * this.maxDistance;
	}
	
	public Panel(Tower tower, PanelType panel) {
		this.tower = tower;
		this.height = panel.getHeight();
		this.minTilt = Angle.fromDegrees(panel.getMintilt());
		this.maxTilt = Angle.fromDegrees(panel.getMaxtilt());
		Angle minAzimuth = norm360(Angle.fromDegrees(panel.getMinazimuth()));
		Angle maxAzimuth = norm360(Angle.fromDegrees(panel.getMaxazimuth()));
		this.sector = new AngularSector(minAzimuth, maxAzimuth);
		this.maxDistance = panel.getMaxdistance() * 1000.0; // meters;
		this.maxDistanceSqared = this.maxDistance * this.maxDistance;
	}
	
	/** 
	 * 
	 * @param distance in meters
	 * @return
	 */
	public static Panel create360Panel(Tower tower, double height, double distance) {
		// TODO contst to config
//		double TOWER_HEIGHT = 30.0;
//		int MAX_PTP_DISTANCE = 50; 
		return new Panel(tower, height, Angle.NEG90, Angle.POS90, AngularSector.FULL_CIRCLE, distance);
	}
	
	
	private static Angle norm360(Angle a) {
		if (a.degrees < 0) {
			return a.add(Angle.POS360); 
		} else {
			return a;
		}
	}
	
	public Tower getTower() {
		return tower;
	}
	
	public double getHeight() {
		return height;
	}

	public Angle getMinTilt() {
		return minTilt;
	}

	public Angle getMaxTilt() {
		return maxTilt;
	}

//	public Angle getMinAzimuth() {
//		return minAzimuth;
//	}
//
//	public Angle getMaxAzimuth() {
//		return maxAzimuth;
//	}
	
	public AngularSector getAngularSector() {
		return sector;
	}
	
	/**
	 * 
	 * @return maximum coverage distance in meters
	 */
	public double getMaxDistance() {
		return maxDistance;
	}
	
	public Vec4 getCenter(Globe globe) {
		if (center != null) {
			return center;
		}
		
		LatLon loc = tower.getLocation();
		double elevation = globe.getElevation(loc.latitude, loc.longitude)
			+ getHeight(); // in meters
		
		center = globe.computePointFromPosition(loc, elevation);
		return center;
	}
	
	
	public boolean isCovered(LatLon loc, Vec4 point, Globe globe) {
		return qualityAt(loc, point, globe) > 0.0;
	}
	
	/**
	 * Check if given location is covered with this panel
	 * @param loc
	 * @return
	 */
	public double qualityAt(LatLon loc, Vec4 point, Globe globe) {
		Vec4 panelPoint = getCenter(globe);
		double distSquared = panelPoint.distanceToSquared3(point);
		if (distSquared > maxDistanceSqared) {
			return 0;
		}
		
		Angle azimuth = LatLon.greatCircleAzimuth(tower.getLocation(), loc);
		if (azimuth.degrees < 0) {
			azimuth = azimuth.add(Angle.POS360);
		}
		
		if (!sector.inside(azimuth)) {
			return 0;
		}
		
		// 90 meters is SRTM resolution, no point making less sample?
		double dist = panelPoint.distanceTo3(point);
		
		if (dist > 0.0) {
			Vec4 isec = 
				RayCastingSupport.intersectSegmentWithTerrain(globe, panelPoint, point, 100, 100);
			if (isec != null) {
				return 0;
			}
		}
		
		return 1.0 - (dist / maxDistance);
	}
	
	@Override
	public String toString() {
		return "Panel: height=" + getHeight() + ", distance=" + getMaxDistance();
	}
}
