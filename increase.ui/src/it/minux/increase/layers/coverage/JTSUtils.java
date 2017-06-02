package it.minux.increase.layers.coverage;

import java.util.List;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class JTSUtils {

	private static final GeometryFactory GF = new GeometryFactory();
	
	public static Coordinate toCoordinate(LatLon loc) {
		return new Coordinate(loc.latitude.degrees, loc.longitude.degrees);
	}
	
	public static Envelope toEnvelope(Sector s) {
		return new Envelope(
				s.getMinLatitude().degrees, 
				s.getMaxLatitude().degrees, 
				s.getMinLongitude().degrees, 
				s.getMaxLongitude().degrees);
	}
	
	public static Coordinate[] toCoordinatesArray(List<? extends LatLon> loc) {
		Coordinate[] result = new Coordinate[loc.size()];
		
		int i = 0;
		for (LatLon l : loc) {
			result[i++] = toCoordinate(l);
		}
		return result;
	}
	
	public static Polygon toPolygon(List<? extends LatLon> loc) {
		LinearRing ring = GF.createLinearRing(toCoordinatesArray(loc));
		return GF.createPolygon(ring, null);
	}

	public static Point toPoint(LatLon loc) {
		return GF.createPoint(toCoordinate(loc));
	}
}
