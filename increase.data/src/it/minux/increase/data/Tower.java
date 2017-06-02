package it.minux.increase.data;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import it.minux.increase.xml.PanelType;
import it.minux.increase.xml.TowerDetailsType;
import it.minux.increase.xml.TowerType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Tower 
	extends PointObject
{
	
	private final String id;
//	private final LatLon location;
	private final double cost;
	private final List<Panel> panels;
	
	private double maxDistance = -1.0;
	
	public Tower(TowerType tower, TowerDetailsType towerDetails) {
		super(LatLon.fromDegrees(tower.getLatitude(), tower.getLongitude()));
				
		if (towerDetails == null) {
			throw new NullPointerException("towerDetails");
		}
		
		if (!tower.getId().equals(towerDetails.getId())) {
			throw new IllegalArgumentException("Different tower ids: " + tower.getId() + ", " + towerDetails.getId());
		}
		
		this.id = tower.getId();
		this.cost = tower.getCost().doubleValue();
		this.panels = createPanels(towerDetails);
	}
	
	public Tower(TowerType tower) {
		super(LatLon.fromDegrees(tower.getLatitude(), tower.getLongitude()));
		
		this.id = tower.getId();
		this.cost = tower.getCost().doubleValue();
		this.panels = Collections.unmodifiableList(new ArrayList<Panel>(0));
	}

	public boolean hasPanels() {
		return !panels.isEmpty();
	}
	
	private List<Panel> createPanels(TowerDetailsType towerDetails) {
		List<PanelType> panelsData = towerDetails.getPanel();
		List<Panel> panels = new ArrayList<Panel>(panelsData.size());

		for (PanelType panelData : panelsData) {
			panels.add(new Panel(this, panelData));
		}
		
		return Collections.unmodifiableList(panels);
	}

	public String getId() {
		return id;
	}

	public double getCost() {
		return cost;
	}
	
	public List<Panel> getPanels() {
		return panels;
	}
	
	@Override
	public String toString() {
		return getId() + " " + getLocation().toString();
	}
	
	public boolean isCovered(LatLon loc, Globe globe) {
		double elevation = globe.getElevation(loc.latitude, loc.longitude);
		Vec4 point = globe.computePointFromPosition(loc, elevation);
		return isCovered(loc, point, globe);
	}
	
	/**
	 * Check if given location is covered with this tower
	 * @param loc
	 * @return
	 */
	public boolean isCovered(LatLon loc, Vec4 point, Globe globe) {
		for (Panel panel : panels) {
			if (panel.isCovered(loc, point, globe)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Compute maximum of panels maxdistances
	 * @return
	 */
	public double getMaxDistance() {
		if (maxDistance < 0) {
			maxDistance = 0.0;
			for (Panel p : panels) {
				double d = p.getMaxDistance();
				if (d > maxDistance) {
					maxDistance = d;
				}
			}
		}
		return maxDistance;
 	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Tower)) {
			return false;
		}
		
		Tower that = (Tower)other;
		
		return this.getId().equals(that.getId());
	}
	
	@Override
	public int hashCode() {
		return this.getId().hashCode();
	}
}
