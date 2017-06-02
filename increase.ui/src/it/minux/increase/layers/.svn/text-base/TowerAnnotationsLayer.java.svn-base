package it.minux.increase.layers;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.AnnotationLayer;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.FrameFactory;
import gov.nasa.worldwind.render.GlobeAnnotation;
import it.minux.increase.data.Tower;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.util.Collection;
import java.util.List;

public class TowerAnnotationsLayer 
	extends AnnotationLayer
{

	private static final AnnotationAttributes attrs = new AnnotationAttributes();
	private Collection<Tower> towers = null;
	private boolean skipTowerWithNoPanel = false;
	
	public TowerAnnotationsLayer(Collection<Tower> towers) {
		this.towers = towers;
	}
	

	public boolean isSkipTowerWithNoPanel() {
		return skipTowerWithNoPanel;
	}

	public void setSkipTowerWithNoPanel(boolean skipTowerWithNoPanel) {
		this.skipTowerWithNoPanel = skipTowerWithNoPanel;
	}
	
	public void init() {
		boolean dontSkip = !skipTowerWithNoPanel;
		setupAttributes(attrs);
		if (towers != null) {
			for (Tower tower : towers) {
				if (dontSkip || tower.hasPanels()) {
					createTowerPoint(tower);
				}
			}
		}
		
		towers = null;
	}

	
	protected void setupAttributes(AnnotationAttributes attrs) {
        attrs = new AnnotationAttributes();
        // Define an 8x8 square centered on the screen point
        attrs.setFrameShape(FrameFactory.SHAPE_RECTANGLE);
        attrs.setLeader(FrameFactory.LEADER_NONE);
        attrs.setAdjustWidthToText(AVKey.SIZE_FIXED);
        attrs.setSize(new Dimension(5, 5));
        attrs.setDrawOffset(new Point(0, 0));
        attrs.setInsets(new Insets(0, 0, 0, 0));
        attrs.setBorderWidth(0);
        attrs.setCornerRadius(0);
        attrs.setBackgroundColor(Color.BLUE);    // Normal color
//        attrs.setTextColor(Color.GREEN);         // Highlighted color
        attrs.setHighlightScale(1.2);
        attrs.setDistanceMaxScale(1);            // No distance scaling
        attrs.setDistanceMinScale(1);
        attrs.setDistanceMinOpacity(1);
	}

	private void createTowerPoint(Tower tower) { 
		TowerAnnotation ann = new TowerAnnotation(tower);
		addAnnotation(ann);
	}
	
	public static TowerAnnotation createTowerAnnotation(Tower tower) {
		return new TowerAnnotation(tower);
	}
	
	public static class TowerAnnotation 
		extends GlobeAnnotation
	{
		private TowerAnnotation(Tower tower) {
			// zero altitude here, since single tower can have multiple panels with multiple altitudes
			super(tower.getId(), new Position(tower.getLocation(), 0.0));
			setAttributes(attrs);
		}
	}
}
