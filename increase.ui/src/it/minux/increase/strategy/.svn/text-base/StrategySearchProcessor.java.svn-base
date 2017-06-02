package it.minux.increase.strategy;

import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.TextArea;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.IndexHits;

import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Extent;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfacePolyline;
import it.minux.increase.data.Tower;
import it.minux.increase.data.TowerSet;
import it.minux.increase.ui.Config;
import it.minux.increase.visibility.AdvSingleSourceShortestPathDijkstra;
import it.minux.increase.visibility.VisibilityGraph;
import it.minux.increase.visibility.VisibilityGraph.TowerRelationships;

public class StrategySearchProcessor
	implements Runnable
{
	
	private static final Log LOG = LogFactory.getLog(StrategySearchProcessor.class);
	
	private final VisibilityGraph vg;
	private final TowerSet towers;
	private final TowerSet towersInTopology;
	private final RenderableLayer debugLayer;
	private final WorldWindowGLCanvas wwg;

	private JFrame resultFrame = null;
	private TextArea resultFrameTextArea = null;

	// FIXME: these constants shouldn't be here. As a temporary workaround, I copied them from VisibilityGraph.java
	private static final String PROP_TOWER_ID = "Id";
	private static final String PROP_TOWER_LATITUDE = "lat";
	private static final String PROP_TOWER_LONGITUDE = "lon";
	private static final String PROP_R_SCORE = "score";
	private static final String PROP_TOWER_IS_DEPLOYED = "deployed";
	
	public StrategySearchProcessor(VisibilityGraph vg, TowerSet towers, TowerSet towersInTopology, RenderableLayer debugLayer, WorldWindowGLCanvas wwg) {
		this.vg = vg;
		this.towers = towers;
		this.towersInTopology = towersInTopology;
		this.debugLayer = debugLayer;
		this.wwg = wwg;
	}

	@Override
	public void run() {
		LOG.debug("begin of search!");
		LOG.debug("Source size: " + towersInTopology.size() + " towers");
		vg.recalculateStrategies();
		LOG.debug("end of search!");
	}
	
	public void suggestNextMove(int steps) {
		String message;
		int i = 1;
		double maxLat = -99999.99;
		double maxLon = -99999.99;
		double minLat = 99999.99;
		double minLon = 99999.99;
		
		message = "Next " + steps + " suggested move(s):\n";
		message += "------------------------------------------\n";
		
		while(steps > 0) {
			// Get the top node from L:
			Node suggestedNode = this.vg.suggestNextMove();
			if(suggestedNode == null) {
				message += "No more moves can be suggested.";
				return;
			}
			Tower suggestedTower = towers.getTowerById((String) suggestedNode.getProperty(PROP_TOWER_ID));
			
			// Get its predecessor on R:
			Node predecessorNode = suggestedNode.getSingleRelationship(TowerRelationships.R_CONNECTED, Direction.INCOMING).getStartNode();
			Tower predecessorTower = towers.getTowerById((String) predecessorNode.getProperty(PROP_TOWER_ID));
			
			// Generate message for pop up window
			message += i + "] ";
			message += "Deploy " + suggestedNode.getProperty(PROP_TOWER_ID);
			message += " and link it to " + predecessorNode.getProperty(PROP_TOWER_ID);
			message += " (score: " + suggestedNode.getProperty(PROP_R_SCORE) + ")";
			message += "\n";
						
			// Show tower on map:
			addSimpleAnnotation(suggestedTower, Color.GREEN);
			
			// if predecessor is an already deployed tower, add an annotation in blue:
			if(towersInTopology.contains((String) predecessorNode.getProperty(PROP_TOWER_ID))) {
				addSimpleAnnotation(predecessorTower, Color.CYAN);
			}
			
			// Show path on map
			displayLinkOnMap(predecessorTower, suggestedTower);
			
			// Keep account of the min/max Lat/Lon, so that we can later zoom in
			// FIXME: there must be a more clever way to do this :)
			if(suggestedTower.getLocation().getLatitude().degrees > maxLat) maxLat = suggestedTower.getLocation().getLatitude().degrees;
			if(predecessorTower.getLocation().getLatitude().degrees > maxLat) maxLat = predecessorTower.getLocation().getLatitude().degrees;
			if(suggestedTower.getLocation().getLatitude().degrees < minLat) minLat = suggestedTower.getLocation().getLatitude().degrees;
			if(predecessorTower.getLocation().getLatitude().degrees < minLat) minLat = predecessorTower.getLocation().getLatitude().degrees;
			if(suggestedTower.getLocation().getLongitude().degrees > maxLon) maxLon = suggestedTower.getLocation().getLongitude().degrees;
			if(predecessorTower.getLocation().getLongitude().degrees > maxLon) maxLon = predecessorTower.getLocation().getLongitude().degrees;
			if(suggestedTower.getLocation().getLongitude().degrees < minLon) minLon = suggestedTower.getLocation().getLongitude().degrees;
			if(predecessorTower.getLocation().getLongitude().degrees < minLon) minLon = predecessorTower.getLocation().getLongitude().degrees;

			steps--;
			i++;
		}
		message += "\n";
		
		// Zoom to the suggested link:
		//LOG.debug("Zooming to range: ("+minLon+"; "+minLat+") "+" ("+maxLon+"; "+maxLat+")");
		Sector sector = Sector.boundingSector(LatLon.fromDegrees(minLat, minLon), LatLon.fromDegrees(maxLat, maxLon));
		Globe globe = wwg.getView().getGlobe();
		double ve = wwg.getSceneController().getVerticalExaggeration();
		double[] minAndMaxElevations = globe.getMinAndMaxElevations(sector);
		Extent extent = Sector.computeBoundingCylinder(globe, ve, sector, minAndMaxElevations[0], minAndMaxElevations[1]);
		double zoom = extent.getRadius() / (wwg.getView().getFieldOfView().tanHalfAngle() * wwg.getView().getFieldOfView().cosHalfAngle());
		zoom = zoom * 1.1; // add a 10% extra space
		wwg.getView().goTo(new Position(sector.getCentroid(), 0.00), zoom);
		
		// Open window with results:
		openResultWindow(message);
	}
	
	private void displayLinkOnMap(Tower t1, Tower t2) {
		if(t1 == null | t2 == null)
			return;
		
		SurfacePolyline line = new SurfacePolyline();
		ArrayList<LatLon> locs = new ArrayList<LatLon>(2);
		locs.add(t1.getLocation());
		locs.add(t2.getLocation());
		line.setLocations(locs);
		line.setAttributes(pathLineAtts);
			
//		line.getAttributes().setOutlineOpacity(0.8);
//		line.getAttributes().setOutlineWidth(5);
//		line.getAttributes().setOutlineMaterial(new Material(Color.ORANGE));
			
		debugLayer.addRenderable(line);
	}
	
	private static final ShapeAttributes pathLineAtts = new BasicShapeAttributes() {
		{
			setOutlineOpacity(0.8);
			setOutlineWidth(5);
			setOutlineMaterial(new Material(Color.ORANGE));
		}
	};
	
	private void addSimpleAnnotation(Tower t, Color color) {
		GlobeAnnotation annTower = new GlobeAnnotation(
				t.getId(),
				new Position(t.getLocation(), 0.0)
			);

		annTower.getAttributes().setOpacity(0.6);
		annTower.getAttributes().setInsets(new Insets(4, 2, 2, 4));
		annTower.getAttributes().setCornerRadius(4);
		annTower.getAttributes().setLeaderGapWidth(14);
		annTower.getAttributes().setDrawOffset(new java.awt.Point(10, 30));
		
		annTower.getAttributes().setBackgroundColor(color);
		debugLayer.addRenderable(annTower);		
	}
	
	private void openResultWindow(String message) {
		// if window doesn't exist, create it:
		if(resultFrame == null) {
			resultFrame = new JFrame("Strategy search");
			resultFrame.setSize(640, 480);
			resultFrame.setVisible(true);
			
			Container content = resultFrame.getContentPane();
		    content.setBackground(Color.white);
	    
		    content.setLayout(new GridLayout(1, 1)); 
	    
		    resultFrameTextArea = new TextArea();
		    resultFrameTextArea.setBackground(Color.WHITE);
		    resultFrameTextArea.setEditable(false);
		    resultFrameTextArea.setText(message);
		    
			content.add(resultFrameTextArea);
		} else {
			// if window exist already, append text:
			resultFrameTextArea.append(message);
		}
		
	    resultFrame.validate();
	}


}
