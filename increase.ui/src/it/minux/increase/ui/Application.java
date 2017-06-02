package it.minux.increase.ui;

import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.examples.ApplicationTemplate;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.UserFacingIcon;
import gov.nasa.worldwind.util.measure.MeasureTool;
import gov.nasa.worldwind.util.measure.MeasureToolController;
import it.minux.increase.data.CoverageRequest;
import it.minux.increase.data.CoverageRequestsLoader;
import it.minux.increase.data.InstallationFailure;
import it.minux.increase.data.InstallationFailuresLoader;
import it.minux.increase.data.SupportRequest;
import it.minux.increase.data.SupportRequestsLoader;
import it.minux.increase.data.Tower;
import it.minux.increase.data.TowerSet;
import it.minux.increase.data.TowersLoader;
import it.minux.increase.data.UserLocation;
import it.minux.increase.data.UsersLocationsLoader;
import it.minux.increase.layers.CombinedHeatmapLayer;
import it.minux.increase.layers.CoverageRequestsLayer;
import it.minux.increase.layers.IMapSelectable;
import it.minux.increase.layers.InstallationFailuresLayer;
import it.minux.increase.layers.NetworkCoverageLayer;
import it.minux.increase.layers.SupportRequestsLayer;
import it.minux.increase.layers.TowerDetailsLayer;
import it.minux.increase.layers.TowerLocationsLayer;
import it.minux.increase.layers.TowerNamesLayer;
import it.minux.increase.layers.UsersLocationsLayer;
import it.minux.increase.layers.TowerLocationsLayer.ITowerDetailsHandler;
import it.minux.increase.layers.coverage.CoverageMap;
import it.minux.increase.layers.coverage.TowersTree;
import it.minux.increase.strategy.StrategySearchProcessor;
import it.minux.increase.utils.TowerFilters;
import it.minux.increase.visibility.VisibilityGraph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;

public class Application extends ApplicationTemplate {
	
	private static final Log LOG = LogFactory.getLog(Application.class);

	public static class AppFrame extends ApplicationTemplate.AppFrame {
		private static final long	serialVersionUID	= 1L;
		
		private TowerSet towers = null;
		private TowerSet towersInTopology = null;
		private TowersTree towersTree = null;
		
		private TowerLocationsLayer towerLocationsLayer;
		private TowerNamesLayer towerNamesLayers;
		private NetworkCoverageLayer coverageMapLayer;
		
		private CoverageRequestsLayer coverageRequestsLayer;
		private UsersLocationsLayer usersLocationsLayer;
		private SupportRequestsLayer supportRequestsLayer;
		private InstallationFailuresLayer installRequestsLayer;
		
		private CombinedHeatmapLayer combinedHeatmapLayer;
		private RenderableLayer graphLayer;
		
		private RenderableLayer targetedIncreaseLayer = new RenderableLayer();
		private RenderableLayer strategySearchLayer = new RenderableLayer();
		
		private VisibilityGraph visibilityGraph;
		
		private JToggleButton targetedIncreaseButton;
		
		private JButton	computeCoverageButton;
		private JButton computeGraphButton;
		
		private JButton recalculateStrategiesButton;
		private JButton suggestNextMoveButton;
		
		private MeasureTool measureTool;
		
		private PropertyChangeListener currentMeasureToolListener = null;
		
		private StrategySearchProcessor strategySearch = null;
		
		public AppFrame() {
			super(true, true, false);
			

			measureTool = new MeasureTool(this.getWwd());
            measureTool.setController(new MeasureToolController());
            measureTool.setMeasureShapeType(MeasureTool.SHAPE_ELLIPSE);
			
			// Update layer panel
			this.getLayerPanel().update(getWwd());

//			JPanel modesPanel = new JPanel(new GridLayout(0, 1, 0, 0));
//			modesPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), 
//					new TitledBorder("Operation Modes")));
			
			
			// Add operations control panel
			JPanel operationsPanel = new JPanel(new GridLayout(0, 1, 0, 0));
			operationsPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), 
					new TitledBorder("Database operations")));			
			
			computeCoverageButton = new JButton("Recalculate coverage");
			computeCoverageButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent actionEvent) {
					int n = JOptionPane.showConfirmDialog(AppFrame.this, 
							"Network coverage will be re-calculated, this may take a long time. Are you sure?",
							"Network coverage ",
							JOptionPane.YES_NO_OPTION);
					
					if (0 == n) {
						updateNetworkCoverage();
					}
					
				}
			});
			operationsPanel.add(computeCoverageButton);
			
			//
			computeGraphButton = new JButton("Recalculate intervisibility");
			computeGraphButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent actionEvent) {
					int n = JOptionPane.showConfirmDialog(AppFrame.this, 
							"Visibility graph will be re-calculated, this may take a long time. Are you sure?",
							"Visibility graph",
							JOptionPane.YES_NO_OPTION);
					
					if (0 == n) {
						updateInterVisibility();
					}
				}
			});
			operationsPanel.add(computeGraphButton);
			
			targetedIncreaseButton = new JToggleButton("Targeted increase");
			targetedIncreaseButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent actionEvent) {
					runTargetedIncrease();
				}
			});
			operationsPanel.add(targetedIncreaseButton);

			recalculateStrategiesButton = new JButton("Recalculate Strategies");
			recalculateStrategiesButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent actionEvent) {
					runStrategySearch();
				}
			});
			operationsPanel.add(recalculateStrategiesButton);

			suggestNextMoveButton = new JButton("Suggest next move");
			suggestNextMoveButton.setEnabled(false);
			suggestNextMoveButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent actionEvent) {
					String answer = (String) JOptionPane.showInputDialog(AppFrame.this, 
							"How many moves to suggest?",
							"Strategy search", 
							JOptionPane.QUESTION_MESSAGE,
							null,
							null,
							"1");

					// if the request is not cancelled:
					if(answer == null || answer == "")
						return;
				
					int n;
					// try parsing the answer:
					try {
						n = Integer.parseInt(answer);
					} catch (NumberFormatException e) {
						JOptionPane.showMessageDialog(AppFrame.this, 
								"Please provide a valid number of moves",
								"Strategy search", 
								JOptionPane.ERROR_MESSAGE);	
						return;
					} 
					
					// check that n is positive:
					if(n <= 0) {
						JOptionPane.showMessageDialog(AppFrame.this, 
								"Please provide a valid number of moves",
								"Strategy search", 
								JOptionPane.ERROR_MESSAGE);	
						return;	
					}
					
					runSuggestNextMove(n);
				}
			});
			operationsPanel.add(suggestNextMoveButton);

			// Add the control panel to the layer panel
			this.getLayerPanel().add(operationsPanel, BorderLayout.SOUTH);
			
			loadData();
			
			getWwd().addSelectListener(mapSelectionListener);
        }
		
		private final SelectListener mapSelectionListener = new SelectListener() {

			@Override
			public void selected(SelectEvent event) {
				if (event.getTopObject() instanceof IMapSelectable) {
					if (SelectEvent.ROLLOVER == event.getEventAction()) {
						((IMapSelectable)event.getTopObject()).onHover();
					} else if (SelectEvent.LEFT_CLICK == event.getEventAction()) {
						((IMapSelectable)event.getTopObject()).onClick();
					}
				}
			}
		};

		
		private void loadData() {
			loadTowers();
			loadUsersLocationsRequests();
			loadCoverageRequests();
			
			loadInstallationFailures();
			loadSupportRequests();
			
			createCombinedHeatmap();
			createVisibilityGraph();
			//createWhiteList();
		}

		//private void createWhiteList() {
			//WhiteList 
		//}

		private void createVisibilityGraph() {
			visibilityGraph = new VisibilityGraph(
					getWwd().getModel().getGlobe(), 
					towers, 
					towersInTopology, 
					combinedHeatmapLayer.getHeatmap());
			visibilityGraph.init();
			
			processVisibilityGraph();
		}
		
		private void updateInterVisibility() {
			if (graphLayer != null) {
				graphLayer.removeAllRenderables();
				getWwd().getModel().getLayers().remove(graphLayer);
				graphLayer = null;
				
				System.gc();
			}
			
			Globe globe = getWwd().getModel().getGlobe();
			visibilityGraph.reinit();
			visibilityGraph.create();
			
			processVisibilityGraph();
		}
		
		private void processVisibilityGraph() {
			if (Config.INSTANCE.getBooleanValue(Config.ENABLE_VISIBILITY_GRAPH_LAYER, false)) {
				graphLayer = visibilityGraph.getGraphLayer();
				if (graphLayer != null) {
					graphLayer.setEnabled(false);
					insertBeforePlacenames(getWwd(), graphLayer);
		            this.getLayerPanel().update(this.getWwd());
				}
			}
		}

		private void createCombinedHeatmap() {
			combinedHeatmapLayer =  
				new CombinedHeatmapLayer(coverageRequestsLayer, supportRequestsLayer, installRequestsLayer);
			
			combinedHeatmapLayer.setEnabled(false);
			insertBeforePlacenames(getWwd(), this.combinedHeatmapLayer);
            this.getLayerPanel().update(this.getWwd());
		}

		private void loadUsersLocationsRequests() {
			try {
				 File xmlLocations = new File(Config.INSTANCE.getValue(Config.USERSLOCATIONS_XMLFILENAME, "data/usersLocations.xml"));
				 LOG.info("Loading " + xmlLocations);
				 List<UserLocation> locations = 
					 UsersLocationsLoader.INSTANCE.loadFromXML(xmlLocations);
				 LOG.info("Loaded, total " + locations.size() + " users locations");
				 
				// Prepare the heatmap for the level of demand
				usersLocationsLayer =  new UsersLocationsLayer(locations);
				usersLocationsLayer.setEnabled(false);
				insertBeforePlacenames(getWwd(), this.usersLocationsLayer);
	            this.getLayerPanel().update(this.getWwd());
			} catch (Exception e) {
				LOG.error("Failed to load users locations", e);
			}
		}

		
		private void loadSupportRequests() {
			try {
				 File xmlRequests = new File(Config.INSTANCE.getValue(Config.SUPPORTREQUESTS_XMLFILENAME, "data/supportRequests.xml"));
				 LOG.info("Loading " + xmlRequests);
				 List<SupportRequest> requests = 
					 SupportRequestsLoader.INSTANCE.loadFromXML(xmlRequests);
				 LOG.info("Loaded, total " + requests.size() + " support requests");
				 
				// Prepare the heatmap for the level of demand
				supportRequestsLayer =  new SupportRequestsLayer(requests, usersLocationsLayer.getHeatmap());
				supportRequestsLayer.setEnabled(false);
				insertBeforePlacenames(getWwd(), this.supportRequestsLayer);
	            this.getLayerPanel().update(this.getWwd());
			} catch (Exception e) {
				LOG.error("Failed to load support requests", e);
			}
		}

		private void loadInstallationFailures() {
			try {
				 File xmlRequests = new File(Config.INSTANCE.getValue(Config.INSTALLATIONFAILURES_XMLFILENAME, "data/installationFailures.xml"));
				 LOG.info("Loading " + xmlRequests);
				 List<InstallationFailure> requests = 
					 InstallationFailuresLoader.INSTANCE.loadFromXML(xmlRequests);
				 LOG.info("Loaded, total " + requests.size() + " installation failures");
				 
				// Prepare the heatmap for the level of demand
				 installRequestsLayer =  new InstallationFailuresLayer(requests, usersLocationsLayer.getHeatmap());
				 installRequestsLayer.setEnabled(false);
				insertBeforePlacenames(getWwd(), this.installRequestsLayer);
	            this.getLayerPanel().update(this.getWwd());
			} catch (Exception e) {
				LOG.error("Failed to load support requests", e);
			}
		}

		private void loadTowers() {
			try {
				File xmlTowers = new File(Config.INSTANCE.getValue(Config.TOWERS_XMLFILENAME, "data/towers.xml"));
				File xmlTopology = new File(Config.INSTANCE.getValue(Config.TOPOLOGY_XMLFILENAME, "data/topology.xml"));
				LOG.info("Loading " + xmlTowers + " and " + xmlTopology);
				towers = TowersLoader.INSTANCE.loadFromXML(xmlTowers, xmlTopology);
				towersInTopology = towers.subset(TowerFilters.WITH_PANELS);
				
				LOG.info("Loaded, total " + towers.size() + " towers, " + towersInTopology.size() + " in topology");
				
				Globe globe = getWwd().getModel().getGlobe();
				towersTree = new TowersTree();
				towersTree.insertTowers(towers, globe);
				
				createTowerLocationsLayer(towers);
				createTowerCoverage(towers);
			} catch (Exception e) {
				LOG.error("Failed to load towers", e);
			}
		}

		private void createTowerCoverage(Collection<Tower> towers) {
			Sector sector = IncreaseConfig.INSTANCE.getSectorOfInteres();
			
//			BufferedImage image = computeCoverage(towers, globe, sector);
			File file = IncreaseConfig.INSTANCE.getCoverageFile();
			if (file != null && file.exists()) {
				try {
					BufferedImage image = ImageIO.read(file);
					coverageMapLayer = new NetworkCoverageLayer(sector, image); 
					insertBeforePlacenames(getWwd(), coverageMapLayer);
					this.getLayerPanel().update(this.getWwd());
				} catch (IOException e) {
					LOG.error("Failed to load coverage file " + file, e);
				}
			}
		}
		
		// intentionally in UI thread
		private void updateNetworkCoverage() {
			if (towers == null) {
				LOG.error("towers.xml not loaded");
				return ;
			}
			
			Sector sector = IncreaseConfig.INSTANCE.getSectorOfInteres();
			Globe globe = getWwd().getModel().getGlobe();
			
			int latres = Config.INSTANCE.getIntValue(Config.HEATMAP_LATRES, 100);
			int lonres = Config.INSTANCE.getIntValue(Config.HEATMAP_LONRES, 100);
			
			Date start = new Date();
			CoverageMap coverage = new CoverageMap(globe, towersTree, sector, latres, lonres);
			coverage.computeCoverage();
			double[] map = coverage.getMap();
			Date end = new Date();
			
			LOG.info("BENCHMARKING. Coverage map created in " + (end.getTime() - start.getTime()) + "ms");
			
			BufferedImage image = createCoverageImage(coverage, map);
			saveCoverageImage(image);
			
			if (coverageMapLayer != null) {
				getWwd().getModel().getLayers().remove(coverageMapLayer);	
				coverageMapLayer = null;
			}
			
			coverageMapLayer = new NetworkCoverageLayer(sector, image); 
			insertBeforePlacenames(getWwd(), coverageMapLayer);
			this.getLayerPanel().update(this.getWwd());
		}

		private BufferedImage createCoverageImage(CoverageMap coverage,
				double[] map) {
			BufferedImage image = new BufferedImage(coverage.getWidth(), coverage.getHeight(), 
					BufferedImage.TYPE_4BYTE_ABGR);
			Color c;
			boolean coverageMonochrome = Config.INSTANCE.getBooleanValue(Config.COVERAGE_MONOCHROME, false);
			if(coverageMonochrome) {
				c = new Color((int) Config.INSTANCE.getIntValue(Config.COVERAGE_MONOCHROME_R, 0),
							  (int) Config.INSTANCE.getIntValue(Config.COVERAGE_MONOCHROME_G, 255),
							  (int) Config.INSTANCE.getIntValue(Config.COVERAGE_MONOCHROME_B, 0), 
							  (int) Config.INSTANCE.getIntValue(Config.COVERAGE_MONOCHROME_A, 128)
							);
			} else {
				c = null;
			}
			
//			float hueScaleFactor = .7f;
			for (int x = 0; x < image.getWidth(); x++) {
				for (int y = 0; y < image.getHeight(); y++) {
					double q = map[y * image.getWidth() + x];
					float hue = (float)(1 - q) / 2; // 1->0: red to green
					float shade = 0;
					if (q > 0) {
						if(!coverageMonochrome) {
							c = new Color(Color.HSBtoRGB(hue, 1f, 1f - shade));
							c = new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(255 * (Math.sqrt(q))));
						} 
						image.setRGB(x, y, c.getRGB());
					} else {
						image.setRGB(x, y, new Color(0f, 0f, 0f, shade).getRGB());
					}
				}
			}
			return image;
		}

		private void saveCoverageImage(BufferedImage image) {
			try {
				File file = new File("coverage.png");
				if (file.exists()) {
					file.delete();
				}
				ImageIO.write(image, "png", file);
			} catch (Exception e) {
				LOG.error("Failed to save coverage", e);
			}
		}

		private void createTowerLocationsLayer(Collection<Tower> towers) {
			towerLocationsLayer = new TowerLocationsLayer(towers, towerDetailsHandler);
			towerNamesLayers = new TowerNamesLayer(towers);
			
			// hide:
			// 1. user may not want to see labels by default
			// 2. performance loss, maybe should upgrade to newer WWJ
			boolean showLabels = Config.INSTANCE.getBooleanValue(Config.TOWERS_LABELS_SHOW, true);
			towerNamesLayers.setEnabled(showLabels); 
			
			boolean skipNoPanels = Config.INSTANCE.getBooleanValue(Config.TOWERS_SKIP_NO_PANELS, false);
			towerLocationsLayer.setSkipTowerWithNoPanel(skipNoPanels);
			towerNamesLayers.setSkipTowerWithNoPanel(skipNoPanels);
			
			towerLocationsLayer.init();
			towerNamesLayers.init();
			
			insertBeforePlacenames(getWwd(), this.towerLocationsLayer);
			insertBeforePlacenames(getWwd(), this.towerNamesLayers);
            this.getLayerPanel().update(this.getWwd());
		}
		
		private final ITowerDetailsHandler towerDetailsHandler = new ITowerDetailsHandler() {

			private TowerDetailsLayer detailsLayer = null;
			private String lastTowerId = null;
			
			@Override
			public void onShowTowerDetails(String towerId, boolean verbose) {
				if (detailsLayer != null) {
					getWwd().getModel().getLayers().remove(detailsLayer);
					detailsLayer = null;
				}
				
				if (lastTowerId != null && towerId.equals(lastTowerId)) { 
					lastTowerId = null;
				} else {
					Tower tower = towers.getTowerById(towerId);
					detailsLayer = new TowerDetailsLayer(tower, visibilityGraph, verbose);
					getWwd().getModel().getLayers().add(detailsLayer);
					
					lastTowerId = towerId;
				}
 			}
			
//			@Override
//			public void onHideTowerDetails() {
//				if (detailsLayer != null) {
//					getWwd().getModel().getLayers().remove(detailsLayer);
//					detailsLayer = null;
//				}
//			}
		};


		private void loadCoverageRequests() {
			try {
				File xmlRequests = new File(Config.INSTANCE.getValue(Config.COVERAGEREQUESTS_XMLFILENAME, "data/coverageRequests.xml"));
				LOG.info("Loading " + xmlRequests);
				List<CoverageRequest> requests = 
					CoverageRequestsLoader.INSTANCE.loadFromXML(xmlRequests);
				LOG.info("Loaded, total " + requests.size() + " coverage requests");	
				
				// Prepare the heatmap for the level of demand
				coverageRequestsLayer =  new CoverageRequestsLayer(requests);
				coverageRequestsLayer.setEnabled(false);
				insertBeforePlacenames(getWwd(), this.coverageRequestsLayer);
	            this.getLayerPanel().update(this.getWwd());
			} catch (Exception e) {
				LOG.error("Failed to load requests", e);
			}
		}
		
		void runTargetedIncrease() {
			LOG.debug("runTargetedIncrease()");
			
			if (targetedIncreaseButton.isSelected()) {
				measureTool.clear();
				measureTool.setArmed(true);
				measureTool.setShowAnnotation(false);
				measureTool.setShowControlPoints(true);

				targetedIncreaseLayer.removeAllRenderables();
				if (!getWwd().getModel().getLayers().contains(targetedIncreaseLayer)) {
					getWwd().getModel().getLayers().add(targetedIncreaseLayer);
				}
				
				removeMeasureToolListener();
				currentMeasureToolListener = 
					new TargetedIncreaseProcessor(measureTool, towers, 
							combinedHeatmapLayer.getHeatmap(), visibilityGraph, 
							targetedIncreaseLayer);
				measureTool.addPropertyChangeListener(currentMeasureToolListener);
			} else {
				if (currentMeasureToolListener instanceof TargetedIncreaseProcessor) {
					((TargetedIncreaseProcessor)currentMeasureToolListener).run();
				}
				
				measureTool.setArmed(false);
//				measureTool.clear();
				measureTool.setShowAnnotation(false);
				measureTool.setShowControlPoints(false);
				
				removeMeasureToolListener();
			}
		}
		
		private void removeMeasureToolListener() {
			if (currentMeasureToolListener != null) {
				measureTool.removePropertyChangeListener(currentMeasureToolListener);
				currentMeasureToolListener = null;
			}
		}
		
		private void runStrategySearch() {
			LOG.debug("runStrategySearch()");
			if(strategySearch == null) {
				strategySearchLayer.removeAllRenderables();
				if (!getWwd().getModel().getLayers().contains(strategySearchLayer)) {
					getWwd().getModel().getLayers().add(strategySearchLayer);
				}
				
				strategySearch = new StrategySearchProcessor(visibilityGraph, towers, towersInTopology, strategySearchLayer, getWwd());
			}
			strategySearch.run();

			
			// enable the button "suggest next move"
			suggestNextMoveButton.setEnabled(true);
		}
		
		private void runSuggestNextMove(int n) {
			LOG.debug("runSuggestNextMove("+n+")");
			strategySearch.suggestNextMove(n);
		}
		
		public void shutdown() {
			visibilityGraph.shutdown();
		}
	}

	public static void main(String[] args) {
		// Set up a simple configuration that logs on the console.
		BasicConfigurator.configure();
		
		try {
			IncreaseConfig.INSTANCE.init(new File(Config.DEFAULT_FILE));
		} catch (Exception e) {
			LOG.error("Failed to load config", e);
		}
		final AppFrame frame = (AppFrame) ApplicationTemplate.start("Incremental Planning Tool", AppFrame.class);
//		frame.shutdown();
		frame.addWindowListener(new WindowListener() {
			
			@Override
			public void windowOpened(WindowEvent arg0) {
			}
			
			@Override
			public void windowIconified(WindowEvent arg0) {
			}
			
			@Override
			public void windowDeiconified(WindowEvent arg0) {
			}
			
			@Override
			public void windowDeactivated(WindowEvent arg0) {
			}
			
			@Override
			public void windowClosing(WindowEvent arg0) {
				frame.shutdown();
			}
			
			@Override
			public void windowClosed(WindowEvent arg0) {
			}
			
			@Override
			public void windowActivated(WindowEvent arg0) {
			}
		});
	}

}
