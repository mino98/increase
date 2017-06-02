package it.minux.increase.layers;

import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.FrameFactory;
import it.minux.increase.data.Tower;
import it.minux.increase.ui.Config;

import java.awt.Insets;
import java.awt.Point;
import java.util.Collection;

public class TowerNamesLayer 
	extends TowerAnnotationsLayer
{

	public TowerNamesLayer(Collection<Tower> towers) {
		super(towers);
		setName("Tower Names");
		int maxAlt = Config.INSTANCE.getIntValue(Config.TOWERS_LABELS_MAXALT, 100000);
		setMaxActiveAltitude(maxAlt);
	}
	
	@Override
	protected void setupAttributes(AnnotationAttributes attrs) {
		attrs.setOpacity(0.6);
		attrs.setFrameShape(FrameFactory.SHAPE_RECTANGLE);	
		attrs.setInsets(new Insets(2, 2, 2, 2));
		attrs.setDrawOffset(new Point(0, 16));
		attrs.setCornerRadius(2);
		attrs.setLeaderGapWidth(12);
	}
}
