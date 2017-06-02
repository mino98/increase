package it.minux.increase.layers.coverage;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.globes.Globe;
import it.minux.increase.data.DataLoadException;
import it.minux.increase.data.Panel;
import it.minux.increase.data.Tower;
import it.minux.increase.data.TowerSet;
import it.minux.increase.data.TowersLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class TowersTreeTest extends TestCase {
	
	public void testQueries() throws DataLoadException {
		File towersFile = new File("data/towers.xml");
		File topologyFile = new File("data/topology.xml");
		
		TowerSet towers = TowersLoader.INSTANCE.loadFromXML(towersFile, topologyFile);
		
		TowersTree tree = new TowersTree();
		Globe globe = new Earth();
		tree.insertTowers(towers, globe);
		
		{
			// at least E-CDF
			List<Panel> foundPanels = tree.query(LatLon.fromDegrees(45.863410, 8.780194));
			List<Tower> foundTowers = toTowers(foundPanels);
			assertTrue("No towers found", !foundTowers.isEmpty());
			assertTowerFound("E-CDF", foundTowers);
			assertTrue("Too many towers found", foundTowers.size() < 100);
		}
		
		{
			// at E-MCAP and E-MINE
			List<Panel> foundPanels = tree.query(LatLon.fromDegrees(44.5267, 11.4086));
			List<Tower> foundTowers = toTowers(foundPanels);
			assertTrue("No towers found", !foundTowers.isEmpty());
			assertTowerFound("E-MCAP", foundTowers);
			assertTowerFound("E-MINE", foundTowers);
			assertTrue("Too many towers found", foundTowers.size() <= 2);
		}
		
		{
			// at least E-CDF
			LatLon loc = LatLon.fromDegrees(44.9330, 8.6256);
			List<Panel> foundPanels = tree.query(loc);
			List<Tower> foundTowers = toTowers(foundPanels);
			assertTrue("No towers found", !foundTowers.isEmpty());
			assertTowerFound("E-BRIC", foundTowers);
//			assertTowerFound("E-MINE", foundTowers);
			assertTrue("Too many towers found", foundTowers.size() <= 10);
			
			Tower tower = getTowerById("E-BRIC", foundTowers);
			boolean covered = tower.isCovered(loc, globe);
			assertTrue(covered);
		}
	}
	
	private List<Tower> toTowers(List<Panel> panels) {
		List<Tower> result = new ArrayList<Tower>();
		for (Panel p : panels) {
			if (!result.contains(p.getTower())) {
				result.add(p.getTower());
			}
		}
		return result;
	}

	private Tower getTowerById(String id, List<Tower> foundTowers) {
		for (Tower t : foundTowers) {
			if (id.equals(t.getId())) {
				return t;
			}
		}
		return null;
	}

	private void assertTowerFound(String id, List<Tower> foundTowers) {
//		boolean isFound = false;
//		for (Tower t : foundTowers) {
//			if (id.equals(t.getId())) {
//				isFound = true;
//			}
//		}
//		
		assertNotNull(id + " not found", getTowerById(id, foundTowers));
	}

}
