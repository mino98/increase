package it.minux.increase.layers.coverage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.strtree.STRtree;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Globe;
import it.minux.increase.data.Panel;
import it.minux.increase.data.Tower;
import it.minux.increase.geom.AngularSector;

public class TowersTree {

	private final STRtree tree = new STRtree();
	
	public TowersTree() {
	}
	
 	public void insertAll(List<Panel> panels, Globe globe) {
 		for (Panel p : panels) {
 			insert(p, globe);
 		}
 	}
 	

	public void insertTowers(Collection<Tower> towers, Globe globe) {
		for (Tower t : towers) {
			insertAll(t.getPanels(), globe);
		}
	}
 	
	public void insert(Panel panel, Globe globe) {
		Envelope env = createEnvelope(panel, globe);
		tree.insert(env, panel);
	}
	
//	private boolean inside(AngularSector a, AngularSector p) {
////		return (a1.compareTo(p1) >=0 && a1.compareTo(p2) <= 0) 
////			&& (a2.compareTo(p1) >=0 && a2.compareTo(p2) <= 0);
//		return p.inside(a);
//	}
	
	private Envelope createEnvelope(Panel panel, Globe globe) {
		Sector bounds = Sector.boundingSector(globe, panel.getTower().getLocation(), 
				panel.getMaxDistance());
		
		bounds = reduceBounds(panel, bounds);
		Envelope env = JTSUtils.toEnvelope(bounds);
		return env;
	}

	
	private static final Angle POS270 = Angle.fromDegrees(270);
	
	private static final AngularSector H1 = new AngularSector(Angle.ZERO, Angle.POS180);
	private static final AngularSector H2 = new AngularSector(Angle.POS90, POS270);
	private static final AngularSector H3 = new AngularSector(Angle.POS180, Angle.POS360);
	private static final AngularSector H4 = new AngularSector(POS270, Angle.POS90);
	
	private static final AngularSector Q1 = new AngularSector(Angle.ZERO, Angle.POS90);
	private static final AngularSector Q2 = new AngularSector(Angle.POS90, Angle.POS180);
	private static final AngularSector Q3 = new AngularSector(Angle.POS180, POS270);
	private static final AngularSector Q4 = new AngularSector(POS270, Angle.POS360);

	private Sector reduceBounds(Panel panel, Sector bounds) {
//		Angle a1 = panel.getMinAzimuth();
//		Angle a2 = panel.getMaxAzimuth();

		AngularSector a = panel.getAngularSector();
		Sector[] parts = bounds.subdivide(); 
		// assume order:
		// 2 | 3
		// 0 | 1
		
		if (H1.inside(a)) {
			if (Q1.inside(a)) {
				bounds = parts[3];
			} else if (Q2.inside(a)) {
				bounds = parts[1];
			}
			bounds = parts[3].union(parts[1]);
		} else if (H2.inside(a)) {
			if (Q2.inside(a)) {
				bounds = parts[1];
			} else if (Q3.inside(a)) {
				bounds = parts[0];
			}
			bounds = parts[1].union(parts[0]);
		} else if (H3.inside(a)) {
			if (Q3.inside(a)) {
				bounds = parts[0];
			} else if (Q4.inside(a)) {
				bounds = parts[2];
			}
			bounds = parts[0].union(parts[2]);
		} else if (H4.inside(a)) {
			if (Q4.inside(a)) {
				bounds = parts[2];
			} else if (Q1.inside(a)) {
				bounds = parts[3];
			}
			bounds = parts[2].union(parts[3]);
		}
		
		return bounds;
	}

	/**
	 * Quickly lookup Towers that MAY cover given point
	 * @param loc
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Panel> query(LatLon loc) {
		Coordinate coord = JTSUtils.toCoordinate(loc);
		Envelope env = new Envelope(coord); 
		List<?> result = tree.query(env);
		return (List<Panel>)result; // dirty cast
	}
	
	private static double INITIAL_RADIUS = 1000.0;
	private static double RADIUS_COEFF = Math.sqrt(2.0);
	private static final int MAX_RESIZES = 2 * 15; // 2^15 * 1000 m

	/**
	 * Lookup n nearest towers
	 * @param loc
	 * @param n
	 * @return
	 */
	public List<Panel> queryNearest(Globe globe, LatLon loc, int  n) {
		if (n < 1) {
			throw new IllegalArgumentException("Illegal number of panels to find: " + n);
		}
		
		double radius = INITIAL_RADIUS; // meters
		int i = 0;
		
		List<Panel> foundPanels = new ArrayList<Panel>(0);
		while (i < MAX_RESIZES) {
			foundPanels = queryInRectangle(globe, loc, radius);
			if (foundPanels.size() >= n) {
				break;
			}
			radius *= RADIUS_COEFF;
			i++;
		}
		
		Collections.sort(foundPanels, new DistanceToPointComaparator(loc));
		int maxIdx = Math.min(foundPanels.size(), n);
		return foundPanels.subList(0, maxIdx);
		
	}
	
	/**
	 * Lookup all towers that intersect with circle of given radius 
	 * @param loc
	 * @param radius
	 * @return
	 */
	public List<Panel> queryInRectangle(Globe globe, LatLon center, double radius) {
		Sector bounds = Sector.boundingSector(globe, center, 
			radius);
		Envelope env = JTSUtils.toEnvelope(bounds);
		
		List<?> result = tree.query(env);
		return (List<Panel>)result; // dirty cast
	}

	public static TowersTree create360Panels(Globe globe, Collection<Tower> towers, double panelHeight, double panelDistance) {
		TowersTree simpleTree = new TowersTree();
		for (Tower t : towers) {
			Panel panel = Panel.create360Panel(t, panelHeight, panelDistance);
			simpleTree.insert(panel, globe);
		}
		
		return simpleTree;
	}
	
	/**
	 * Unwrap small list of Panels to list of Towers
	 * @param panels
	 * @return
	 */
	public static List<Tower> toTowers(List<Panel> panels) {
		List<Tower> result = new ArrayList<Tower>();
		
		for (Panel p : panels) {
			if (!result.contains(p.getTower())) {
				result.add(p.getTower());
			}
		}
		
		return result;
	}
}
