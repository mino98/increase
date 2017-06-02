package it.minux.increase.visibility;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.SurfacePolyline;
import it.minux.increase.data.Panel;
import it.minux.increase.data.Tower;
import it.minux.increase.data.TowerSet;
import it.minux.increase.layers.CombinedHeatmap;
import it.minux.increase.layers.CombinedHeatmap.Area;
import it.minux.increase.layers.HeatmapPoint;
import it.minux.increase.layers.coverage.TowersTree;
import it.minux.increase.ui.Config;
import it.minux.increase.ui.IncreaseConfig;
import it.minux.increase.ui.Application.AppFrame;
import it.minux.increase.utils.TowerFilters;
import it.minux.increase.strategy.NodeComparator;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.neo4j.graphalgo.CostEvaluator;
import org.neo4j.graphalgo.EstimateEvaluator;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphalgo.impl.util.DoubleAdder;
import org.neo4j.graphalgo.impl.util.DoubleComparator;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Expander;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.TraversalPosition;
import org.neo4j.graphdb.Traverser;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.index.RelationshipIndex;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.Traversal;

public class VisibilityGraph {

	private static final String DB_PATH = "var/graphdb";
	private static final String PROP_TOWER_ID = "Id";
	private static final String PROP_TOWER_C = "c(t)";
	private static final String PROP_TOWER_H = "h(m)";
	private static final String PROP_TOWER_LATITUDE = "lat";
	private static final String PROP_TOWER_LONGITUDE = "lon";

	private static final String PROP_LOC_INDEX = "idx"; // index in heatmap
	private static final String PROP_LOC_VALUE = "value";
	
	private static final String PROP_R_SCORE = "score";
	private static final String PROP_TOWER_IS_DEPLOYED = "deployed";
	
	// private static final String PROP_VISIBLE_IDX = "vis_idx"; // indexes of
	// visible heatmap points

	private static final String PROP_REL_IDS = "Ids";

	private static final Log LOG = LogFactory.getLog(VisibilityGraph.class);
	
	private static NodeComparator nodeComparator = new NodeComparator();
	private static PriorityQueue<Node> priorityQueueL = new PriorityQueue<Node>(11, nodeComparator);
	
	private final AdvCostEvaluator<Double> costEval = new AdvCostEvaluator<Double>() {

		@Override
		public Double getCost(Relationship relationship, Direction direction,
				List<Node> previosNodes) {
			Node n1 = relationship.getStartNode();
			Node n2 = relationship.getEndNode();

			Tower t2 = allTowers.getTowerById((String) n2
					.getProperty(PROP_TOWER_ID));

			// if (LOG.isTraceEnabled()) {
			// LOG.debug(t1.getId() + " -> " + t2.getId());
			// }

			// double hm = 0;
			// if (n2.hasProperty(PROP_TOWER_H)) {
			// hm = (Double)n2.getProperty(PROP_TOWER_H);
			// } else {
			// double panelHeight = IncreaseConfig.INSTANCE.getPtpHeight();
			// double panelDistance =
			// IncreaseConfig.INSTANCE.getMaxPtpDistance();
			// Sector s = Sector.boundingSector(globe, t2.getLocation(),
			// panelDistance);
			// Area area = combinedHeatmap.getArea(s);
			//
			// if (area != null) {
			// Panel panel = Panel.create360Panel(t2, panelHeight,
			// panelDistance);
			// for (HeatmapPoint point : area) {
			// LatLon loc = point.getLocation();
			// double elevation = globe.getElevation(loc.latitude,
			// loc.longitude);
			// Vec4 v4 = globe.computePointFromPosition(loc, elevation + 2.0);
			//
			// if (panel.isCovered(loc, v4, globe)) {
			// hm += point.getValue();
			// }
			// }
			// }
			// n2.setProperty(PROP_TOWER_H, new Double(hm));
			// }

			Map<Integer, Node> heatmapPoints = collectHeatmapPoints(n2);

			for (Node prevNode : previosNodes) {
				removeDuplicates(heatmapPoints, prevNode);
			}

			double f = computeCostFunction(t2, heatmapPoints);
			return f;
		}

	};
	
	/**
	 * Remove heatmap points that are visible from give tower node
	 * 
	 * @param heatmapPoints
	 *            - collection of heatmap points to filter
	 * @param n1
	 *            - tower node
	 */
	private void removeDuplicates(Map<Integer, Node> heatmapPoints, Node n1) {
		for (Relationship r : n1.getRelationships(
				TowerRelationships.LOC_IS_VISIBLE, Direction.OUTGOING)) {
			Node locNode = r.getOtherNode(n1);
			Integer idx = (Integer) locNode.getProperty(PROP_LOC_INDEX);
			if (heatmapPoints.containsKey(idx)) {
				heatmapPoints.remove(idx);
			}
		}
	}

	private final EstimateEvaluator<Double> estimateEval = new EstimateEvaluator<Double>() {

		private final double minCost;

		{
			// TODO
			minCost = Config.INSTANCE.getFloatValue("astar.a0", 100.0f);

		}

		@Override
		public Double getCost(Node node, Node goal) {

			Tower t1 = allTowers.getTowerById((String) node
					.getProperty(PROP_TOWER_ID));
			Tower t2 = allTowers.getTowerById((String) goal
					.getProperty(PROP_TOWER_ID));

			LatLon l1 = t1.getLocation();
			LatLon l2 = t2.getLocation();

			Angle angle = LatLon.greatCircleDistance(l1, l2);
			double radius = globe.getRadiusAt(l1);
			double distance = angle.radians * radius;
			double panelDistance = IncreaseConfig.INSTANCE.getMaxPmpDistance();

			double n = distance / (2 * panelDistance);

			return minCost * n;
		}
	};

	// CommonEvaluators.geoEstimateEvaluator(
	// PROP_LATITUDE, PROP_LONGITUDE );

	private final Globe globe;
	private final TowerSet allTowers;
	private final TowerSet towersInTopology;
	private final CombinedHeatmap combinedHeatmap;

	private TowerSet whitelist = null;

	public enum TowerRelationships implements RelationshipType {
		IS_VISIBLE, // A -IS_VISIBLE-> B ~ B is visible by A
		LOC_IS_VISIBLE, // T -LOC_IS_VISIBLE-> LOC ~ heatmap location in visible
						// by tower
		R_CONNECTED, 
		// A -R_CONNECTED-> B ~ A is connected to B on the minimum-cost
		// tree R
	}

	private GraphDatabaseService graphDb = null;
	private Index<Node> indexTowerNodes = null;
	private Index<Node> indexHeatmapNodes = null;
	private RelationshipIndex indexRNodes = null;
	private RelationshipIndex indexTowerVisibility = null;
	// private In

	private RenderableLayer debugLayer = new RenderableLayer();

	private final double c0;
	private final double h0;
	private final double a0;

	{
		c0 = Config.INSTANCE.getFloatValue("astar.c0", 1.0f);
		h0 = Config.INSTANCE.getFloatValue("astar.h0", 0.001f);
		a0 = Config.INSTANCE.getFloatValue("astar.a0", 100.0f);
	}

	public VisibilityGraph(Globe globe, TowerSet allTowers,
			TowerSet towersInTopology, CombinedHeatmap combinedHeatmap) {
		this.globe = globe;
		this.allTowers = allTowers;
		this.towersInTopology = towersInTopology;
		this.combinedHeatmap = combinedHeatmap;
	}

	/**
	 * Try to load existing graph, if not create new
	 * 
	 * @return true if loaded
	 */
	public boolean init() {
		if (graphDb != null) {
			throw new IllegalStateException("Graph already inited");
		}

		graphDb = new EmbeddedGraphDatabase(DB_PATH);

		Transaction tx = graphDb.beginTx();
		try {
			// all Neo4j operations that work with the graph
			// ...

			IndexManager index = graphDb.index();
			indexTowerNodes = index.forNodes("Towers");
			indexHeatmapNodes = index.forNodes("Heatmap");
			indexTowerVisibility = index.forRelationships("Visibility");
			indexRNodes = index.forRelationships("R");
			tx.success();
		} finally {
			tx.finish();
		}

		return true;
	}

	public void shutdown() {
		if (graphDb != null) {
			graphDb.shutdown();
			graphDb = null;
		}
	}

	public void create() {
		if (graphDb == null) {
			if (!init()) {
				return;
			}
		}

		double panelHeight = IncreaseConfig.INSTANCE.getPtpHeight();
		double panelDistance = IncreaseConfig.INSTANCE.getMaxPtpDistance();
		TowersTree simpleTree = TowersTree.create360Panels(globe, allTowers,
				panelHeight, panelDistance);

		Transaction tx = graphDb.beginTx();
		try {
			Date start = new Date();
			tx = computeVisibility(tx, allTowers, globe, simpleTree);
			Date end = new Date();
			LOG.info("BENCHMARKING. Intervisibility graph created in " + (end.getTime() - start.getTime()) + "ms");
			tx.success();
		} finally {
			tx.finish();
		}

		graphDb.shutdown();
		graphDb = null;
		System.gc();
		init();
	}

	private double computeCostFunction(Tower tower,
			Map<Integer, Node> heatmapPoints) {
		double hm = 0;
		for (Node locNode : heatmapPoints.values()) {
			Object obj = locNode.getProperty(PROP_LOC_VALUE);
			if (obj instanceof Double) {
				hm += ((Double) obj).doubleValue();
			}
		}
		double f = a0 + (c0 * tower.getCost()) - (h0 * hm);
		return f;
	}
	
	private Transaction computeVisibility(Transaction tx, TowerSet towers,
			Globe globe, TowersTree simpleTree) {
		int n = towers.size();
		int i = 0;

		for (Tower t : towers) {
			List<Panel> possibleVisible = simpleTree.query(t.getLocation());

			Sector s = computeTowerBounds(t, globe);
			ElevationHelper.INSTANCE.ensureElevationLoaded(s, globe);
			for (Panel p : possibleVisible) {
				if (t != p.getTower()) {
					checkVisible(t, p, globe);
				}
			}

			createVisibleHeatmapPoints(globe, t, s);

			i++;
			if ((i % 10) == 0) {
				LOG.info(i + " of " + n + " towers processed");
			}

			if ((i % 100) == 0) {
				// commit transaction and start new one
				tx.success();
				tx.finish();
				tx = graphDb.beginTx();
			}
		}

		return tx;
	}

	private void createVisibleHeatmapPoints(Globe globe, Tower t, Sector s) {
		{
			double panelHeight = IncreaseConfig.INSTANCE.getPtpHeight();
			double panelDistance = IncreaseConfig.INSTANCE.getMaxPtpDistance();
			Panel panel = Panel.create360Panel(t, panelHeight, panelDistance);
			Area area = combinedHeatmap.getArea(s);
			if (area != null) {
				Node nodeTower = getNodeForTower(t);
				for (HeatmapPoint point : area) {
					LatLon loc = point.getLocation();
					double elevation = globe.getElevation(loc.latitude,
							loc.longitude);
					Vec4 v4 = globe.computePointFromPosition(loc,
							elevation + 2.0);

					if (panel.isCovered(loc, v4, globe)) {
						Node nodePoint = getNodeForHeatmapPoint(point);
						Relationship r = nodeTower.createRelationshipTo(
								nodePoint, TowerRelationships.LOC_IS_VISIBLE);
					}
				}
			}
		}
	}

	private Sector computeTowerBounds(Tower t, Globe globe) {
		double distance = IncreaseConfig.INSTANCE.getMaxPtpDistance();
		if (!t.getPanels().isEmpty()) {
			distance = t.getPanels().get(0).getMaxDistance();
		}

		Sector s1 = Sector.boundingSector(globe, t.getLocation(), distance);
		return s1;
	}

	// private final Map<String, Node> towerNodeCache = new HashMap<String,
	// Node>();

	public Node findNodeForTower(String towerId) {
		IndexHits<Node> hits = indexTowerNodes.get(PROP_TOWER_ID, towerId);
		Node single = hits.getSingle();
		if (single != null) {
			return single;
		}

		return null;
	}

	private Node findNodeForHeatmapPoint(int index) {
		IndexHits<Node> hits = indexHeatmapNodes.get(PROP_LOC_INDEX, index);
		Node single = hits.getSingle();
		if (single != null) {
			return single;
		}

		return null;
	}

	private Node getNodeForHeatmapPoint(HeatmapPoint point) {
		Node single = findNodeForHeatmapPoint(point.getIndex());
		if (single != null) {
			return single;
		}

		Node node = graphDb.createNode();
		node.setProperty(PROP_LOC_INDEX, point.getIndex());
		node.setProperty(PROP_LOC_VALUE, point.getValue());

		indexHeatmapNodes.add(node, PROP_LOC_INDEX, point.getIndex());
		return node;
	}

	public void connectFakeRoot(Node n1, Node fakeroot) {
		
	    	Relationship r = fakeroot.createRelationshipTo(n1, TowerRelationships.IS_VISIBLE);
	    	String key = getRelationshipKey(n1, fakeroot);
	    	indexTowerVisibility.add(r, PROP_REL_IDS, key);
	    	
	}
	
	public void createFakeRoot() {
		// check whether the fake root node already exists:
		Node fakeroot = findNodeForTower("fakeroot");
		if (fakeroot != null) {
			LOG.debug("fakeroot already in place");
			return; // if so, do nothing
		}		

		Transaction tx = graphDb.beginTx();
	    try {
			fakeroot = graphDb.createNode();
			fakeroot.setProperty(PROP_TOWER_ID, "fakeroot");
			fakeroot.setProperty(PROP_TOWER_C, 0);
			fakeroot.setProperty(PROP_TOWER_LATITUDE, 0);
			fakeroot.setProperty(PROP_TOWER_LONGITUDE, 0);
			indexTowerNodes.add(fakeroot, PROP_TOWER_ID, "fakeroot");
			tx.success();
	    } finally {
	    	tx.finish();
	    	LOG.debug("fakeroot created");
	    }
	}
	
	private static final CostEvaluator<Double> testCostEval = 
		new CostEvaluator<Double>() {

			@Override
			public Double getCost(Relationship relationship, Direction direction) {
				return 1.0;
			}
	};
	

	public void recalculateStrategies() {
		Date start, stop;
		
		// Step 0. Create a new fake source node with cost zero, if needed:
		createFakeRoot();
		Node fakeroot = findNodeForTower("fakeroot");
		
		// And connect it to all the source towers. While we do so, we also reset the 
		// PROP_TOWER_IS_DEPLOYED flag.
		Transaction tx = graphDb.beginTx();
	    try {
			for (Tower t1 : allTowers) {
				Node n1 = findNodeForTower(t1.getId());
				
				if(n1 == null) {
					LOG.debug("Skipping tower " + t1.getId());
					continue;
				}

				// If towers has already been deployed:
				if(t1.hasPanels()) {
					connectFakeRoot(n1, fakeroot);
					// LOG.debug("Added link " + t1.getId() + " - fakeroot");
				
					// Also, set this tower as deployed:
					n1.setProperty(PROP_TOWER_IS_DEPLOYED, true);
				} else {
					// If tower has not been deployed already
					n1.setProperty(PROP_TOWER_IS_DEPLOYED, false);
				}
			}
			tx.success();
	    } finally {
	    	tx.finish();
	    }
	    
		// Step 1: run Dijkstra
		LOG.debug("Running Dijkstra path search");
		start = new Date();
		AdvSingleSourceShortestPathDijkstra<Double> dijkstra
		= new AdvSingleSourceShortestPathDijkstra<Double>(0.0, 
				fakeroot, 
//				testCostEval,
				costEval,
				new DoubleAdder(), 
				new DoubleComparator(), 
				Direction.OUTGOING, 
				TowerRelationships.IS_VISIBLE);
	
		boolean ok = dijkstra.calculate();
		if(!ok) {
			LOG.error("Dijkstra ended with errors!");
			return;
		}
		stop = new Date();
		LOG.info("BENCHMARKING. Djikstra: " + (stop.getTime() - start.getTime()) + " ms");
	    
	    // Step 2: delete the relationships that define the R tree
		LOG.debug("Deleting the R tree");
		for (Tower t : allTowers) {
			// Skip invalid towers:
			Node n = findNodeForTower(t.getId());
			if (n == null) {
				LOG.error("No node for tower " + t.getId());
				continue;
			}
			
			Iterator<Relationship> it = n.getRelationships(TowerRelationships.R_CONNECTED).iterator();
			while(it.hasNext()) {
				Relationship r = it.next();
				
				tx = graphDb.beginTx();
			    try {
					indexRNodes.remove(r);
			    	r.delete();
					tx.success();
			    } finally {
			    	tx.finish();
			    	//LOG.debug("deleted from "+n.getProperty(PROP_TOWER_ID));
			    }
			}
		}
		
		// Step 3: generate a new R tree
		LOG.debug("Generating the R tree");
		start = new Date();
		for (Tower t : allTowers) {
			// Skip invalid towers:
			Node n = findNodeForTower(t.getId());
			if (n == null) {
				LOG.error("No node for tower " + t.getId());
				continue;
			}
			
			// Skip towers already deployed:
			if (n.getProperty(PROP_TOWER_IS_DEPLOYED).equals(true)) {
				continue;
			}
			
			// Get the shortest path:
			List<Node> path = dijkstra.getPathAsNodes(n);
//			if (path != null) {
//				Double cost = dijkstra.getCost(n);
//				LOG.info(t.getId() + " is reachable from topology, cost = " + cost);
//				LOG.info(path);
//			} else {
			
			// Skip if tower is unreachable:
			if (path == null) {
				LOG.warn(t.getId() + " is not reachable from topology");
				continue;
			}
			
			// debugging:
			//Double cost = dijkstra.getCost(n);
			//LOG.debug(t.getId() + " total cost = " + cost);
				
			ListIterator<Node> li = path.listIterator(path.size() - 1);
			Node predecessor = n;
			Node successor = n;
			
			while(li.hasPrevious() && !predecessor.hasRelationship(TowerRelationships.R_CONNECTED, Direction.INCOMING)) {
				successor = predecessor;
				predecessor = (Node) li.previous();
				
				// Add it to the tree as a child of predecessor
				if(predecessor != null) {
					tx = graphDb.beginTx();
				    try {
				    	Relationship r = predecessor.createRelationshipTo(successor, TowerRelationships.R_CONNECTED);
						String key = getRelationshipKey(predecessor, successor);
						indexRNodes.add(r, PROP_REL_IDS, key);
						tx.success();
				    } finally {
				    	tx.finish();
				    	//LOG.debug(predecessor.getProperty(PROP_TOWER_ID) + " -> " + successor.getProperty(PROP_TOWER_ID));
				    }
				}
			}
		}
		stop = new Date();
		LOG.info("BENCHMARKING. Building R tree: " + (stop.getTime() - start.getTime()) + " ms");

		
		// Step 4: Traversing the R tree from the root to the leaves to set the scores.
		start = new Date();
		LOG.debug("Traversing tree from root to leaves");
		if (fakeroot == null) {
			LOG.error("fakeroot node not found!");
			return;
		}
		
		// get the interest rate from the configuration:
		double C = IncreaseConfig.INSTANCE.getC();
		
		// breath-first traversal of the tree:
		@SuppressWarnings("deprecation")
		TraversalDescription td = Traversal.description()
			.breadthFirst()
			.relationships(TowerRelationships.R_CONNECTED, Direction.OUTGOING)
			.filter(Traversal.returnAllButStartNode());
			
		for (Path path : td.traverse(fakeroot)) {
			// Update the score:
			// (h(n) - c(n)) / (1+C)^depth
			
			Node n = path.endNode();
			int depth = path.length();
			double cn = (Double) n.getProperty(PROP_TOWER_C);
			double hn = 0.0; 
			
			Map<Integer, Node> heatmapPoints = collectHeatmapPoints(n);
			
			Iterator<Node> pathNodesIt = path.nodes().iterator();
			while (pathNodesIt.hasNext()) {
				Node nodeToExclude = pathNodesIt.next();
				if (pathNodesIt.hasNext()) {
					removeDuplicates(heatmapPoints, nodeToExclude);
				}
			}
				
			for (Node locNode : heatmapPoints.values()) {
				Object obj = locNode.getProperty(PROP_LOC_VALUE);
				if (obj instanceof Double) {
					hn += ((Double) obj).doubleValue();
				}
			}
			
			// We add a fixed positive offset to make sure the result is a positive value:
			double score = 1000 + (hn - cn) / Math.pow(1+C, depth);
			
			tx = graphDb.beginTx();
		    try {
		    	n.setProperty(PROP_R_SCORE, score);
		    	tx.success();
		    } finally {
		    	tx.finish();
		    	LOG.debug("Updated "+n.getProperty(PROP_TOWER_ID)+" h(n)="+hn+" c(n)="+cn+" C="+C+" Depth="+depth+" Score="+score);
		    }
		}
		stop = new Date();
		LOG.info("BENCHMARKING. Settings scores: " + (stop.getTime() - start.getTime()) + " ms");
		
		// Step 5: Traversing the R tree from the leaves to the root to sum the scores.
		LOG.debug("Traversing tree from leaves to root");
		start = new Date();
		sumChildren(fakeroot);	    
		stop = new Date();
		LOG.info("BENCHMARKING. Updating scores: " + (stop.getTime() - start.getTime()) + " ms");
}

	private void generateL(Node n) {
		// for each child of n:
		Iterable<Relationship> iterableChildren = n.getRelationships(TowerRelationships.R_CONNECTED, Direction.OUTGOING);
		Iterator<Relationship> iteratorChildren = iterableChildren.iterator();
		while(iteratorChildren.hasNext()) {
			Node child = iteratorChildren.next().getEndNode();
		
			// if it hasn't already been deployed, add it to L
			if(child.getProperty(PROP_TOWER_IS_DEPLOYED).equals(false)) {
				
				// add to the list
				priorityQueueL.add(child);
				//LOG.debug("Node "+child.getProperty(PROP_TOWER_ID)+" (score: "+child.getProperty(PROP_R_SCORE)+") added to L");
			} else {
				// otherwise, check his children
				generateL(child);
			}
		}
	}
	
	public Node suggestNextMove() {
		Date start, stop;
		
		// Step 1: Empty the next best moves sorted list.
		LOG.debug("(Re)gernerate the next best moves sorted list L");
		priorityQueueL.clear();

		// Step 2: Generate the next best moves sorted list L
		Node fakeroot = findNodeForTower("fakeroot");
		
		start = new Date();
		generateL(fakeroot);
		stop = new Date();
		LOG.info("BENCHMARKING. Generating L list: " + (stop.getTime() - start.getTime()) + " ms");		
		
		// Step 3: Check if priorityQueueL exists:
		if(priorityQueueL.size() == 0) {
			LOG.warn( "No more moves can be suggested.");
			return null;
		}
		
		// Step 4: pop out the first element of L
		Node n = priorityQueueL.poll();
		
		// DEBUGGING:
		//Node predecessor = n.getSingleRelationship(TowerRelationships.R_CONNECTED, Direction.INCOMING).getStartNode();
		//LOG.debug("Deploy " + n.getProperty(PROP_TOWER_ID) +
		//	" and link it to " + predecessor.getProperty(PROP_TOWER_ID) +
		//	" (score: "+n.getProperty(PROP_R_SCORE)+")");
		//
		
		// Step 5: mark the element n as deployed
		Transaction tx = graphDb.beginTx();
	    try {
			n.setProperty(PROP_TOWER_IS_DEPLOYED, true);
			tx.success();
	    } finally {
	    	tx.finish();
	    }

		return n;
	}
	
	
	private double sumChildren(Node n) {		
		LOG.debug("Analyzing " + n.getProperty(PROP_TOWER_ID));
		
		// If this is a leaf node, return its score:
		if(!n.hasRelationship(TowerRelationships.R_CONNECTED, Direction.OUTGOING)) {
			 //LOG.debug("+ it is a leaf");
			return (Double) n.getProperty(PROP_R_SCORE);	
		}

		// Otherwise, we set the score of this node equal to the sum of the score of its children:
		Iterable<Relationship> iterableChildren = n.getRelationships(TowerRelationships.R_CONNECTED, Direction.OUTGOING);
		Iterator<Relationship> iteratorChildren = iterableChildren.iterator();

		double score;
		if(n.hasProperty(PROP_R_SCORE)) {
			score = (Double) n.getProperty(PROP_R_SCORE);	
		} else {
			score = 0.0;
		}
			
		while(iteratorChildren.hasNext()) {
			Relationship r = iteratorChildren.next();
			Node child = r.getEndNode();
			//LOG.debug("+ child "+child.getProperty(PROP_TOWER_ID));
			
			score += sumChildren(child);
		}
		
		// If this is an already deployed node or the root, don't set the score value.
		if(towersInTopology.contains((String) n.getProperty(PROP_TOWER_ID))) {
			 //LOG.debug("+ not setting score of "+n.getProperty(PROP_TOWER_ID)+" because already deployed.");
			 return score;
		} else {
			Transaction tx = graphDb.beginTx();
		    try {
		    	n.setProperty(PROP_R_SCORE, score);
		    	tx.success();
		    } finally {
		    	tx.finish();
				//LOG.debug("+ setting score of "+n.getProperty(PROP_TOWER_ID)+" to " + score);
		    }
			return score;
		}
	}

	private Node getNodeForTower(Tower t) {
		Node single = findNodeForTower(t.getId());
		if (single != null) {
			return single;
		}

		Node node = graphDb.createNode();
		node.setProperty(PROP_TOWER_ID, t.getId());
		node.setProperty(PROP_TOWER_C, t.getCost());
		node.setProperty(PROP_TOWER_LATITUDE,
				t.getLocation().getLatitude().degrees);
		node.setProperty(PROP_TOWER_LONGITUDE,
				t.getLocation().getLongitude().degrees);

		// towerNodeCache.put(t.getId(), node);
		indexTowerNodes.add(node, PROP_TOWER_ID, t.getId());
		return node;
	}

	private void checkVisible(Tower t, Panel p, Globe globe) {
		double panelHeight = 30;
		if (!t.getPanels().isEmpty()) {
			panelHeight = t.getPanels().get(0).getHeight();
		}

		LatLon loc = t.getLocation();
		double elevation = globe.getElevation(loc.latitude, loc.longitude);
		Vec4 point = globe.computePointFromPosition(loc, elevation
				+ panelHeight);

		Node n1 = getNodeForTower(t);
		Node n2 = getNodeForTower(p.getTower());

		if (isDirectlyVisible(n2, n1)) {
			return;
		}

		if (p.isCovered(loc, point, globe)) {
			connectVisibleNodes(n1, n2);
			connectVisibleNodes(n2, n1);
			// n2.createRelationshipTo(n1, TowerRelationships.IS_VISIBLE);
			// indexTowerVisibility
		}
	}

	private void connectVisibleNodes(Node n1, Node n2) {
		Relationship r = n1.createRelationshipTo(n2,
				TowerRelationships.IS_VISIBLE);
		String key = getRelationshipKey(n1, n2);
		indexTowerVisibility.add(r, PROP_REL_IDS, key);
	}

	private String getRelationshipKey(Node n1, Node n2) {
		String key = n1.getProperty(PROP_TOWER_ID) + ":"
				+ n2.getProperty(PROP_TOWER_ID);
		return key;
	}

	public List<Tower> getDirectlyVisibly(Tower src) {
		Node srcNode = findNodeForTower(src.getId());
		if (srcNode == null) {
			// LOG.error("Failed to find node for " + src.getId());
			return null;
		}

		List<Tower> result = new ArrayList<Tower>();
		for (Relationship r : srcNode.getRelationships(
				TowerRelationships.IS_VISIBLE, Direction.OUTGOING)) {
			Node start = srcNode;
			Node end = r.getOtherNode(start);
			Tower dst = allTowers.getTowerById((String) end
					.getProperty(PROP_TOWER_ID));
			result.add(dst);
		}

		return result;
	}

	private boolean isDirectlyVisible(Node n2, Node n1) {
		// for (Relationship r :
		// n2.getRelationships(TowerRelationships.IS_VISIBLE)) {
		// Node end = r.getOtherNode(n2);
		// if (end.equals(n1)) {
		// return true;
		// }
		// }
		//
		// return false;
		String key = getRelationshipKey(n2, n1);
		IndexHits<Relationship> hits = indexTowerVisibility.get(PROP_REL_IDS,
				key);
		if (hits.hasNext()) {
			return true;
		}

		return false;
	}

	// debug only
	public RenderableLayer getGraphLayer() {
		if (graphDb == null) {
			if (!init()) {
				return null;
			}
		}

		debugLayer.setName("Inter-visibility graph");
		debugLayer.removeAllRenderables();
		debugLayer.setPickEnabled(false);
		debugLayer.setMaxActiveAltitude(200000);

		int lines = 0;

		Set<String> processed = new HashSet<String>();

		Iterator<Node> it = graphDb.getAllNodes().iterator();
		while (it.hasNext()) {
			Node node = it.next();

			for (Relationship r : node.getRelationships()) {
				Node end = r.getEndNode();
				Tower t1 = allTowers.getTowerById((String) node
						.getProperty(PROP_TOWER_ID));
				Tower t2 = allTowers.getTowerById((String) end
						.getProperty(PROP_TOWER_ID));

				if (t1 != null && t2 != null) {
					String k1 = t1.getId() + "_" + t2.getId();
					String k2 = t2.getId() + "_" + t1.getId();

					if (!processed.contains(k1) && !processed.contains(k2)) {
						processed.add(k1);

						SurfacePolyline line = new SurfacePolyline();
						ArrayList<LatLon> locs = new ArrayList<LatLon>(2);
						locs.add(t1.getLocation());
						locs.add(t2.getLocation());
						line.setLocations(locs);
						debugLayer.addRenderable(line);

						lines++;
					}
				} else {
					LOG.warn("Missing "
							+ (String) node.getProperty(PROP_TOWER_ID) + " or "
							+ (String) end.getProperty(PROP_TOWER_ID)
							+ " tower");
				}
			}
		}

		LOG.info("Total connections: " + lines);
		return debugLayer;
	}

	public void reinit() {
		if (graphDb != null) {
			graphDb.shutdown();
			graphDb = null;
		}

		File dbDir = new File(DB_PATH);
		deleteDirectory(dbDir);

		init();
	}

	public List<Tower> findPathToNetwork(Tower tower) {
		Node startNode = findNodeForTower(tower.getId());
		if (startNode == null) {
			LOG.error("Tower " + tower.getId()
					+ " not found in visibility graph");
			return null;
		}

		double panelHeight = IncreaseConfig.INSTANCE.getPtpHeight();
		double panelDistance = IncreaseConfig.INSTANCE.getMaxPmpDistance();
		TowersTree topologyTowersTree = TowersTree.create360Panels(globe,
				towersInTopology, panelHeight, panelDistance);

		List<Tower> nearestDeployedTowers = TowersTree
				.toTowers(topologyTowersTree.queryNearest(globe,
						tower.getLocation(), 20));

		WeightedPath bestPath = null;

		Transaction tx = graphDb.beginTx();
		try {
			Expander relExpander = Traversal.expanderForTypes(
					TowerRelationships.IS_VISIBLE, Direction.OUTGOING);
			relExpander.add(TowerRelationships.IS_VISIBLE, Direction.BOTH);
			// PathFinder<WeightedPath> sp = GraphAlgoFactory.aStar(
			// relExpander,
			// costEval, estimateEval );
			AdvAStar astar = new AdvAStar(relExpander, costEval, estimateEval);

			Set<Node> goalNodes = new HashSet<Node>();
			for (Tower goalTower : nearestDeployedTowers) {
				//LOG.debug("Searching path to " + goalTower.getId());
				Node goalNode = getNodeForTower(goalTower);
				goalNodes.add(goalNode);
			}

			WeightedPath path = astar.findSinglePath(startNode, goalNodes);
			if (path != null) {
				//LOG.debug("Found path, cost: " + path.weight());
				if (bestPath == null || bestPath.weight() > path.weight()) {
					bestPath = path;
				}
			}
		} finally {
			tx.finish();
		}
		
		if (bestPath == null) {
			LOG.error("Path not found ");
			return null;
		}

		LOG.debug("BENCHMARKING. Best path cost: " + bestPath.weight());
		
		List<Tower> resultPath = new ArrayList<Tower>();
		for (Node node : bestPath.nodes()) {
			Tower t = allTowers.getTowerById((String) node
					.getProperty(PROP_TOWER_ID));
			if (t == null) {
				LOG.error("Tower not found for id: "
						+ node.getProperty(PROP_TOWER_ID));
				throw new IllegalStateException("Tower not found for id: "
						+ node.getProperty(PROP_TOWER_ID));
			}
			resultPath.add(t);
		}

		return resultPath;
	}

	public TowerSet getWhitelist() {
		if (whitelist == null) {
			whitelist = createWhitelist();
		}

		return whitelist;

	}

	private class TowerWithCost {
		private final Tower tower;
		private final double cost;

		public TowerWithCost(Tower tower, double cost) {
			this.tower = tower;
			this.cost = cost;
		}

		public Tower getTower() {
			return tower;
		}

		public double getCost() {
			return this.cost;
		}
	}

	private final Comparator<TowerWithCost> costComparator = new Comparator<TowerWithCost>() {

		@Override
		public int compare(TowerWithCost t1, TowerWithCost t2) {
			return Double.compare(t1.getCost(), t2.getCost());
		}
	};

	private TowerSet createWhitelist() {
		TowerSet whitelist = new TowerSet();

		TowerSet notInTopology = allTowers.subset(TowerFilters.WITHOUT_PANELS);

		int nWhitelistTargets = Config.INSTANCE.getIntValue(
				"WHITELIST_TARGETS", 10);

		PriorityQueue<TowerWithCost> sorted = new PriorityQueue<TowerWithCost>(
				nWhitelistTargets, costComparator);

		Transaction tx = graphDb.beginTx();
		try {
			for (Tower t : notInTopology) {
				Node n = getNodeForTower(t);
				if (n == null) {
					// LOG.error("Failed to find node for tower " + t.getId());
					continue;
				}

				Map<Integer, Node> heatmapPoints = collectHeatmapPoints(n);
				double cost = computeCostFunction(t, heatmapPoints);
				sorted.add(new TowerWithCost(t, cost));
			}

			tx.success();
		} finally {
			tx.finish();
		}

		for (int i = 0; i < nWhitelistTargets; ++i) {
			TowerWithCost target = sorted.poll();
			LOG.debug("f(" + target.getTower().getId() + ") = "
					+ target.getCost());

			whitelist.addTower(target.getTower());
			// List<Tower> path = findPathToNetwork(target.getTower());
			// if (path != null) {
			// for (int j = 0; j < path.size() - 1; ++j) {
			// whitelist.addTower(path.get(j));
			// }
			// }
		}

		return whitelist;
	}

	private Map<Integer, Node> collectHeatmapPoints(Node n) {
		Map<Integer, Node> heatmapPoints = new HashMap<Integer, Node>(400);
		for (Relationship r : n.getRelationships(
				TowerRelationships.LOC_IS_VISIBLE, Direction.OUTGOING)) {
			Node locNode = r.getOtherNode(n);
			heatmapPoints.put((Integer) locNode.getProperty(PROP_LOC_INDEX),
					locNode);
		}
		return heatmapPoints;
	}

	static public boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}

}
