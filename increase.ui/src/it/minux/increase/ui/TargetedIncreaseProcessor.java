package it.minux.increase.ui;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfacePolyline;
import gov.nasa.worldwind.util.measure.MeasureTool;
import it.minux.increase.data.Panel;
import it.minux.increase.data.Tower;
import it.minux.increase.data.TowerSet;
import it.minux.increase.layers.CombinedHeatmap;
import it.minux.increase.layers.HeatmapPoint;
import it.minux.increase.layers.CombinedHeatmap.Area;
import it.minux.increase.layers.coverage.JTSUtils;
import it.minux.increase.layers.coverage.TowersTree;
import it.minux.increase.visibility.VisibilityGraph;

import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.TextArea;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Runs targeted increase operation mode after user has selected area
 * @author brujito
 *
 */
public class TargetedIncreaseProcessor implements PropertyChangeListener {

	private static final Log LOG = LogFactory.getLog(TargetedIncreaseProcessor.class);
	
	private final Globe globe;
	private final MeasureTool tool;
	private final TowerSet towers;
//	private final TowerSet towersInTopology;
	private final CombinedHeatmap heatmap;
	private final VisibilityGraph visibility;
	private final RenderableLayer debugLayer;
	
	public TargetedIncreaseProcessor(
			MeasureTool tool, 
			TowerSet towers, 
//			TowerSet towersInTopology, 
			CombinedHeatmap heatmap, 
			VisibilityGraph visibility,
			RenderableLayer debugLayer) {
		this.globe = tool.getWwd().getModel().getGlobe();
		this.tool = tool;
		this.towers = towers;
//		this.towersInTopology = towersInTopology;
		this.heatmap = heatmap;
		this.visibility = visibility;
		this.debugLayer = debugLayer;

	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
//		LOG.info(evt.getPropertyName());
//		if (MeasureTool.EVENT_RUBBERBAND_STOP == evt.getPropertyName()) {
//		handleAreaSelected();
//		}
	}
	
	public void run() {
		handleAreaSelected();
	}

	private void handleAreaSelected() {
		List<? extends Position> pos = tool.getPositions();
		
		
		HeatmapPoint hottest = findHottestPoint(pos);
		if (hottest == null) {
//			LOG.error("Hottest point not found");
			openResultWindow("Hottest point not found");
			return ;
		}
		LOG.info("Hottest point: " + hottest.getLocation() + " -> " + hottest.getValue());
		
		
		String valueStr = NumberFormat.getInstance().format(hottest.getValue());
		GlobeAnnotation annHottest = new GlobeAnnotation(valueStr,
				new Position(hottest.getLocation(), 0.0));
		setupSimpleAnnotation(annHottest);
		annHottest.getAttributes().setBackgroundColor(Color.RED);
		debugLayer.addRenderable(annHottest);
	
		List<Tower> nearTowers = findNearTowers(hottest.getLocation());
		if (nearTowers == null || nearTowers.isEmpty()) {
			LOG.error("Nearby towers not found");
			return ;
		}
		
		for (Tower t : nearTowers) {
			LOG.info("Near tower: " + t);
		}
		
		Collections.sort(nearTowers, new NearestComaparator(hottest.getLocation()));
		
		int i = 0;
		for (Tower t : nearTowers) {
			GlobeAnnotation annTower = new GlobeAnnotation(t.getId(),
					new Position(t.getLocation(), 0.0));
			setupSimpleAnnotation(annTower);
			if (i == 0) {
				annTower.getAttributes().setBackgroundColor(Color.GREEN);
			}
			debugLayer.addRenderable(annTower);
			i++;
			break;
		}
		LOG.info("Nearest tower: " + nearTowers.get(0));
		
		List<Tower> path = connectTower(nearTowers.get(0));
		
		if (path != null) {
			displayPathOnMap(path);
			openResultWindow(getResultMessage(path, hottest));
		} else {
			openResultWindow("Path not found");
		}
	}

	private void setupSimpleAnnotation(GlobeAnnotation annTower) {
		annTower.getAttributes().setOpacity(0.6);
		annTower.getAttributes().setInsets(new Insets(4, 2, 2, 4));
		annTower.getAttributes().setCornerRadius(4);
		annTower.getAttributes().setLeaderGapWidth(14);
		annTower.getAttributes().setDrawOffset(new java.awt.Point(10, 30));
	}
	
	private class NearestComaparator implements Comparator<Tower> {
		
		private final LatLon loc;
		
		NearestComaparator(LatLon loc) {
			this.loc = loc;
		}

		@Override
		public int compare(Tower o1, Tower o2) {
			Angle d1 = LatLon.greatCircleDistance(o1.getLocation(), loc);
			Angle d2 = LatLon.greatCircleDistance(o2.getLocation(), loc);
			
			return d1.compareTo(d2);
		}
		
	}

	private List<Tower> findNearTowers(LatLon loc) {
		Globe globe = tool.getWwd().getModel().getGlobe();
		double panelHeight = IncreaseConfig.INSTANCE.getPtpHeight();
		double panelDistance = IncreaseConfig.INSTANCE.getMaxPmpDistance();
		TowersTree simpleTree = TowersTree.create360Panels(globe, towers, panelHeight, panelDistance);
		
		
		List<Panel> panels = simpleTree.query(loc);

		double elevation = globe.getElevation(loc.latitude, loc.longitude);
		Vec4 point = globe.computePointFromPosition(loc, elevation + 2.0);
		
		List<Tower> nearest = new ArrayList<Tower>();
		for (Panel panel : panels) {
			if (panel.isCovered(loc, point, globe)) {
				if (!nearest.contains(panel.getTower())) {
					nearest.add(panel.getTower());
				}
			}
		}
		return nearest;
	}

	private HeatmapPoint findHottestPoint(List<? extends Position> pos) {
		Polygon poly = JTSUtils.toPolygon(pos);
		Sector s = Sector.boundingSector(pos);
		
		Area area = heatmap.getArea(s);
		if (area == null) {
			return null;
		}
		
		
		Iterator<HeatmapPoint> it = area.iterator();
//		Iterator<HeatmapPoint> it = heatmap.iterator();
		HeatmapPoint hottest = null;
		
		while (it.hasNext()) {
			HeatmapPoint next = it.next();
			Point point = JTSUtils.toPoint(next.getLocation());
			if (poly.contains(point)) {
				if (hottest == null || next.getValue() > hottest.getValue()) {
					hottest = next;
				}
			}
		}
 		
		return hottest;
	}
	
	private static final ShapeAttributes pathLineAtts = new BasicShapeAttributes() {
		{
			setOutlineOpacity(0.8);
			setOutlineWidth(5);
			setOutlineMaterial(new Material(Color.ORANGE));
		}
	};
	
	private List<Tower> connectTower(Tower tower) {
		if (tower.hasPanels()) {
//			LOG.info("Nearest tower " + tower.getId() + " is already deployed");
			openResultWindow("Nearest tower " + tower.getId() + " is already deployed");
			return null;
		}
		
		return visibility.findPathToNetwork(tower);

	}

	private String getPathNotFoundMessage() {
		return "PATH NOT FOUND";
	}

	private String getResultMessage(List<Tower> path, HeatmapPoint hottest) {
		StringBuilder builder = new StringBuilder();

		builder.append("BEST STRATEGY TO COVER HOTSPOT ");
		LatLon loc = hottest.getLocation();
		builder.append("(" + loc.getLatitude() + "; " + loc.getLongitude() + ")\n");
		builder.append("New towers to be installed:\n");
//		for (Tower t : path) {
//			builder.append(" - ");
//			builder.append(t.getId());
//			builder.append("\n");
//		}
		
		for (int i = 0; i < path.size() - 1; ++i) {
			builder.append("- ");
			builder.append(path.get(i).getId());
			builder.append("\n");
		}
		
		builder.append("\n");
		builder.append("Strategy to be followed:\n");
		
		int n = path.size();
		Tower prev = path.get(path.size() - 1);
		for (int i = path.size() - 2; i >= 0; --i) {
			Tower t = path.get(i);
			
			builder.append("Step ");
			builder.append(String.valueOf(n - i - 1));
			builder.append(": install tower ");
			builder.append(t.getId());
			builder.append(" (lat: ");
			builder.append(t.getLocation().getLatitude());
			builder.append(" - long: ");
			builder.append(t.getLocation().getLongitude());
			builder.append(") and link it to ");
			builder.append(prev.getId());
			builder.append("\n");
			
			prev = t;
		}
		
		return builder.toString();
	}

	private void displayPathOnMap(List<Tower> path) {
		Iterator<Tower> it = path.iterator();
		Tower t1 = it.next();
		
		while (it.hasNext()) {
			Tower t2 = it.next();
			
			SurfacePolyline line = new SurfacePolyline();
			ArrayList<LatLon> locs = new ArrayList<LatLon>(2);
			locs.add(t1.getLocation());
			locs.add(t2.getLocation());
			line.setLocations(locs);
			line.setAttributes(pathLineAtts);
			
//			line.getAttributes().setOutlineOpacity(0.8);
//			line.getAttributes().setOutlineWidth(5);
//			line.getAttributes().setOutlineMaterial(new Material(Color.ORANGE));
			
			debugLayer.addRenderable(line);
			
			t1 = t2;
		}
	}

	private void openResultWindow(String message) {
		JFrame resultFrame = new JFrame("Targeted increase");
		resultFrame.setSize(640, 480);
		resultFrame.setVisible(true);
		
		Container content = resultFrame.getContentPane();
	    content.setBackground(Color.white);
	    
	    content.setLayout(new GridLayout(1, 1)); 
	    
	    TextArea textArea = new TextArea();
	    textArea.setBackground(Color.WHITE);
	    textArea.setEditable(false);
	    textArea.setText(message);
	    content.add(textArea);
	    
	    resultFrame.validate();
	}

//	private String resultToText(List<Tower> path) {
//		StringBuilder builder = new StringBuilder();
//		
//		
//		return builder.toString();
//	}
}
