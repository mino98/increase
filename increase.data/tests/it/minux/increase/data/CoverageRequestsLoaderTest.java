package it.minux.increase.data;

import static org.junit.Assert.*;

import gov.nasa.worldwind.geom.LatLon;

import java.io.File;
import java.util.List;

import org.junit.Test;

public class CoverageRequestsLoaderTest {

	@Test
	public void testLoadFromCSV() throws DataLoadException {
		File csvFile = new File("xml/coverageRequests.csv");
		List<CoverageRequest> requests = CoverageRequestsLoader.INSTANCE.loadFromCSV(csvFile);
		
		// 7226 requests in file
		assertEquals(7226, requests.size());
		
		CoverageRequest r0 = requests.get(0);
		
		assertEquals(LatLon.fromDegrees(44.395602, 10.918929), r0.getLocation());
		assertNull(r0.getTimestamp());
	}
	
	@Test
	public void testLoadFromXML() throws DataLoadException {
		File xmlFile = new File("xml/coverageRequests.xml");
		List<CoverageRequest> requests = CoverageRequestsLoader.INSTANCE.loadFromXML(xmlFile);
		
		// 7226 requests in file
		assertEquals(7226, requests.size());
		
		CoverageRequest r0 = requests.get(0);
		
		assertEquals(LatLon.fromDegrees(44.395602, 10.918929), r0.getLocation());
		assertNotNull(r0.getTimestamp());
	}

}
