package it.minux.increase.data;

import gov.nasa.worldwind.geom.LatLon;
import it.minux.increase.xml.CoverageRequestType;

import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeFactory;

public class CoverageRequest 
	extends PointEvent
{
	public CoverageRequest(CoverageRequestType request) {
		super(request);
	}
	
	public CoverageRequest(LatLon location, GregorianCalendar timestamp) {
		super(location, timestamp);
	}
	
	public CoverageRequestType toJAXB(DatatypeFactory factory) {
		CoverageRequestType request = new CoverageRequestType();
		
		LatLon loc = getLocation();
		request.setLatitude(loc.getLatitude().degrees);
		request.setLongitude(loc.getLongitude().degrees);
		
		GregorianCalendar ts = getTimestamp();
		if (ts == null) {
			// fallback to now
			ts = new GregorianCalendar(); 
		}
		
		request.setTimestamp(factory.newXMLGregorianCalendar(ts));
 		return request;
	}
}
