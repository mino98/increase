package it.minux.increase.data;

import it.minux.increase.xml.Topology;
import it.minux.increase.xml.TowerDetailsType;
import it.minux.increase.xml.TowerType;
import it.minux.increase.xml.Towers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TowersLoader 
	extends XMLLoader
{

	private static final Log LOG = LogFactory.getLog(TowersLoader.class);
	
	public static final TowersLoader INSTANCE = new TowersLoader();
	
	private TowersLoader() {
	}
	
	public TowerSet loadFromXML(File towersFile, File topologyFile) throws DataLoadException {
		try {
			Topology topology = unmarshallFile(Topology.class, topologyFile);
			Map<String, TowerDetailsType> id2TowerDetail = new HashMap<String, TowerDetailsType>();
			for (TowerDetailsType detail : topology.getTower()) {
//				if (id2TowerDetail.containsKey(detail.getId())) {
//					LOG.warn("Duplicate tower: " + )
//				}
				id2TowerDetail.put(detail.getId(), detail);
			}
			
			Towers towersXML = unmarshallFile(Towers.class, towersFile);
			TowerSet result = new TowerSet(towersXML.getTower().size());
//			Map<String, Tower> index = new HashMap<String, Tower>();
			
			for (TowerType towerData : towersXML.getTower()) {
				TowerDetailsType towerDetail = id2TowerDetail.remove(towerData.getId());
				if (towerDetail != null) {
					Tower tower = new Tower(towerData, towerDetail);
					result.addTower(tower);
				} else {
//					LOG.warn("Missing tower detail for tower: " + towerData.getId());
					Tower tower = new Tower(towerData);
					result.addTower(tower);
				}
			}
			
			for (String id : id2TowerDetail.keySet()) {
				LOG.warn("Missing tower for panels: " + id);
			}
			
			return result;
		} catch (Exception e) {
			throw new DataLoadException("Failed to load towers", e);
		}
	}
	
}
