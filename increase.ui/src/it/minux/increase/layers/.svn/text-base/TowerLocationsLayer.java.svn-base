package it.minux.increase.layers;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.IconLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.UserFacingIcon;
import it.minux.increase.data.Tower;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TowerLocationsLayer 
	extends IconLayer
{
	private final static Log LOG = LogFactory.getLog(TowerLocationsLayer.class);
	
	private Collection<Tower> towers = null;
	
	private final ITowerDetailsHandler detailsHandler;
	private boolean skipTowerWithNoPanel = false;
	
	private Layer towerDetailsLayer = null;
	
	
	public interface ITowerDetailsHandler {
		void onShowTowerDetails(String towerId, boolean verbose);
//		void onHideTowerDetails();
	}
	
	public TowerLocationsLayer(Collection<Tower> towers, ITowerDetailsHandler detailsHandler) {
//		super(towers);
		this.detailsHandler = detailsHandler;
		this.towers = towers;
		setName("Tower Locations");
	}
	
	public boolean isSkipTowerWithNoPanel() {
		return skipTowerWithNoPanel;
	}

	public void setSkipTowerWithNoPanel(boolean skipTowerWithNoPanel) {
		this.skipTowerWithNoPanel = skipTowerWithNoPanel;
	}
	
	public void init() {
		boolean dontSkip = !skipTowerWithNoPanel;
		if (towers != null) {
			for (Tower tower : towers) {
				if (dontSkip || tower.hasPanels()) {
					createTowerPoint(tower);
				}
			}
		}
		
		towers = null;
	}
	
	public class TowerIcon 
		extends UserFacingIcon  
		implements IMapSelectable
	{
		private final String towerId;
		private boolean details = false;
		
		TowerIcon(Tower tower) {
			super(
					(tower.hasPanels()) ? IMG_TOWER_DEPLOYED : IMG_TOWER,
					new Position(tower.getLocation(), 0));
			this.towerId = tower.getId();
			setHighlightScale(2);
		}

		@Override
		public void onClick() {
			// TODO Auto-generated method stub
//			LOG.debug("onClick(" + towerId + ")");
//			showTowerDetails(towerId);
			if (detailsHandler != null) {
				detailsHandler.onShowTowerDetails(towerId, true);
			}
		}

		@Override
		public void onHover() {
			hover(this);
			// TODO Auto-generated method stub
//			LOG.debug("onHover(" + towerId + ")");
//			showTowerDetails(towerId);
//			setHighlighted(true);
//			if (detailsHandler != null) {
//				detailsHandler.onShowTowerDetails(towerId, true);
//			}
		}
	}
	
	private static final String IMG_TOWER = "tower.png";
	private static final String IMG_TOWER_DEPLOYED = "tower_deployed.png";
	
	private TowerIcon hoverIcon = null;
	
	private void hover(TowerIcon icon) {
		if (hoverIcon != null) {
			hoverIcon.setHighlighted(false);
			hoverIcon = null;
		}
		
		hoverIcon = icon;
		hoverIcon.setHighlighted(true);
	}
	
	private void createTowerPoint(Tower tower) { 
		String iconPath = (tower.hasPanels()) ? IMG_TOWER_DEPLOYED : IMG_TOWER;
//		UserFacingIcon icon = new UserFacingIcon(iconPath, 
//				new Position(tower.getLocation(), 0));
//		icon.setValue(, value)
//		icon.setHighlightScale(2);
		TowerIcon icon = new TowerIcon(tower);
		addIcon(icon);
	}

//	@Override
//	protected void setupAttributes(AnnotationAttributes attrs) {
//        // Define an 8x8 square centered on the screen point
//        attrs.setFrameShape(FrameFactory.SHAPE_RECTANGLE);
//        attrs.setLeader(FrameFactory.LEADER_NONE);
//        attrs.setAdjustWidthToText(AVKey.SIZE_FIXED);
//        attrs.setSize(new Dimension(5, 5));
//        attrs.setDrawOffset(new Point(0, 0));
//        attrs.setInsets(new Insets(0, 0, 0, 0));
//        attrs.setBorderWidth(0);
//        attrs.setCornerRadius(0);
//        attrs.setBackgroundColor(Color.BLUE);    // Normal color
////        attrs.setTextColor(Color.GREEN);         // Highlighted color
//        attrs.setHighlightScale(1.2);
//        attrs.setDistanceMaxScale(1);            // No distance scaling
//        attrs.setDistanceMinScale(1);
//        attrs.setDistanceMinOpacity(1);
//	}
//	
//
//	private void showTowerDetails(String towerId) {
//		
//	}
}
