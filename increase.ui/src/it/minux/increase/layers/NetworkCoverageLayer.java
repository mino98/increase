package it.minux.increase.layers;

import java.awt.image.BufferedImage;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.SurfaceImage;

public class NetworkCoverageLayer 
	extends RenderableLayer
{
	public NetworkCoverageLayer(Sector sector, BufferedImage image) {
		setName("Network Coverage");
		setPickEnabled(false);
		
		SurfaceImage surfaceImage = new SurfaceImage(image, sector);
		surfaceImage.setOpacity(.7);
		addRenderable(surfaceImage);
	}
	
}
