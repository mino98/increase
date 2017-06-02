package it.minux.increase.data;

import gov.nasa.worldwind.geom.LatLon;
import it.minux.increase.xml.PointEventType;

import java.util.GregorianCalendar;

public class PointEvent 
	extends PointObject
{
	private final GregorianCalendar timestamp;

	public PointEvent(PointEventType pointEvent) {
		this(LatLon.fromDegrees(pointEvent.getLatitude(), pointEvent.getLongitude()),
			pointEvent.getTimestamp().toGregorianCalendar());
	}
	
	public PointEvent(LatLon location, GregorianCalendar timestamp) {
		super(location);
		this.timestamp = timestamp;
	}

	/**
	 * Nullable
	 * @return
	 */
	public GregorianCalendar getTimestamp() {
		return this.timestamp;
	}
}
