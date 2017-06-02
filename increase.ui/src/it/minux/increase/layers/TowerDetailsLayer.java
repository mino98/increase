package it.minux.increase.layers;

import java.awt.Insets;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import it.minux.increase.data.Tower;
import it.minux.increase.visibility.VisibilityGraph;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.FrameFactory;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.SurfacePolyline;

public class TowerDetailsLayer 
	extends RenderableLayer
{

	private static final AnnotationAttributes attrs = new AnnotationAttributes();
	static {
		attrs.setOpacity(0.6);
		attrs.setFrameShape(FrameFactory.SHAPE_RECTANGLE);	
		attrs.setInsets(new Insets(2, 2, 2, 2));
		attrs.setDrawOffset(new Point(0, 16));
		attrs.setCornerRadius(2);
		attrs.setLeaderGapWidth(12);
	}
	
	private final Tower tower;
	private final VisibilityGraph visibilityGraph;
	
	public TowerDetailsLayer(Tower tower, VisibilityGraph visibilityGraph, boolean verbose) {
		this.tower = tower;
		this.visibilityGraph = visibilityGraph;
		
		setPickEnabled(false);
		
		TowerAnnotation ann = 
			 new TowerAnnotation(tower);
		addRenderable(ann);
		
		if (verbose) {
			List<Tower> visible = visibilityGraph.getDirectlyVisibly(tower);
			if (visible != null) {
				for (Tower t2 : visible) {
					
					SurfacePolyline line = new SurfacePolyline();
					ArrayList<LatLon> locs = new ArrayList<LatLon>(2);
					locs.add(tower.getLocation());
					locs.add(t2.getLocation());
					line.setLocations(locs);
					
					addRenderable(line);
				}
			}
		}
	}
	
	
	public Tower getTower() {
		return tower;
	}
	
	private class TowerAnnotation 
		extends GlobeAnnotation
	{
		private TowerAnnotation(Tower tower) {
			// zero altitude here, since single tower can have multiple panels with multiple altitudes
			super(tower.getId(), new Position(tower.getLocation(), 0.0));
			setAttributes(attrs);
		}
	}
	
}
