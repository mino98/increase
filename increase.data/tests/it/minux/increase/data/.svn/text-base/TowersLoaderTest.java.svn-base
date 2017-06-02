package it.minux.increase.data;

import static org.junit.Assert.assertEquals;
import gov.nasa.worldwind.geom.LatLon;

import java.io.File;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

public class TowersLoaderTest {

	@Test
	public void testLoadTowers() throws DataLoadException {
		File towersFile = new File("xml/towers.xml");
		File topologyFile = new File("xml/topology.xml");
		
		TowerSet towers = TowersLoader.INSTANCE.loadFromXML(towersFile, topologyFile);
		
		assertEquals(2, towers.size());
		
		Iterator<Tower> it = towers.iterator();	
		
		Tower t1 = it.next();
		assertEquals("T0123", t1.getId());
		assertEquals(LatLon.fromDegrees(24.123, 10.123), t1.getLocation());
		assertEquals(new BigDecimal("600.0"), t1.getCost());
		
				
		
		List<Panel> panels = t1.getPanels();
		assertEquals(2, panels.size());
		
		Tower t2 = it.next();
		assertEquals("ABC123", t2.getId());
		
		
	}

}
