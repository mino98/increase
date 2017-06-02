/**
 * 
 */
package it.minux.increase.layers.coverage;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import it.minux.increase.data.Panel;

import java.util.Comparator;

/**
 * Compare to towers by angular distance to point
 * @author brujito
 *
 */
class DistanceToPointComaparator 
	implements Comparator<Panel> {

	private final LatLon loc;
	
	DistanceToPointComaparator(LatLon loc) {
		this.loc = loc;
	}
	
	private Angle distance(LatLon l1, LatLon l2) {
		
		return l1.latitude.angularDistanceTo(l2.latitude).add(
			l1.longitude.angularDistanceTo(l2.longitude));
	}
	
	@Override
	public int compare(Panel p1, Panel p2) {
		LatLon l1 = p1.getTower().getLocation();
		LatLon l2 = p2.getTower().getLocation();
		
		Angle d1 = distance(l1, loc);
		Angle d2 = distance(l2, loc);
		
		return d1.compareTo(d2);
	}
}