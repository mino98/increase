package it.minux.increase.geom;

import gov.nasa.worldwind.geom.Angle;

/**
 * Represents sector enclosed by two azimuths
 * @author brujito
 *
 */
public class AngularSector {

	public static final AngularSector FULL_CIRCLE = new AngularSector(Angle.ZERO, Angle.ZERO);
	
	private final Angle min;
	private final Angle max;
	
	private final boolean fullCircle;
	private final boolean overZero; // zero angle is inside sector
	private AngularSector complementSector = null;
	
	public static AngularSector fromDegrees(double min, double max) {
		return new AngularSector(Angle.fromDegrees(min), Angle.fromDegrees(max));
	}
	
	public AngularSector(Angle min, Angle max) {
		this.min = norm0360(min);
		this.max = norm0360(max);
		
		fullCircle = this.min.degrees == this.max.degrees;
		overZero = this.min.degrees > this.max.degrees;
	}
	
	/**
	 * Get complement sector, e.g. complement(330-30) = 30-330 
	 * @return
	 */
	public final AngularSector complement() {
		if (complementSector == null) {
			complementSector = new AngularSector(max, min);
		}
		return complementSector;
	}
	
	private static Angle norm0360(Angle a) {
		while (a.degrees < 0) {
			a = a.add(Angle.POS360); 
		} 
		
		while (a.degrees > 360) {
			a = a.subtract(Angle.POS360);
		}
		
		return a;
	}
	
	/**
	 * check if other AngularSector is inside this
	 * @param other
	 * @return
	 */
	public boolean inside(AngularSector other) {
		if (other.fullCircle && !this.fullCircle) {
			return false;
		}
		
		if (this.fullCircle) {
			return true;
		}
	
		// XXX check for overZero?
		return inside(other.min) && inside(other.max);
	}

	/**
	 * Is azimuth inside given sector
	 * @param a
	 * @return
	 */
	public boolean inside(Angle a) {
		if (fullCircle) {
			return true;
		}
		
		if (overZero) {
			return !complement().inside(a);
		}
		
		return min.compareTo(a) <= 0
			&& max.compareTo(a) >= 0;
	}
}
