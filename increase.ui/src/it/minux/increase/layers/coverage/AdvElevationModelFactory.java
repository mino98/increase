package it.minux.increase.layers.coverage;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.ogc.OGCConstants;
import gov.nasa.worldwind.terrain.BasicElevationModelFactory;
import gov.nasa.worldwind.util.WWXML;

import org.w3c.dom.Element;

public class AdvElevationModelFactory 
	extends BasicElevationModelFactory
{
	
	@Override
    protected ElevationModel createNonCompoundModel(Element domElement, AVList params)
    {
        ElevationModel em;
        
        String serviceName = WWXML.getText(domElement, "Service/@serviceName");
        if (serviceName.equals(OGCConstants.WMS_SERVICE_NAME)) {
            em = new AdvWMSElevationModel(domElement, params);
        } else if (serviceName.equals("WWTileService")) {
            em = new AdvElevationModel(domElement, params);
        } else {
        	em = super.createNonCompoundModel(domElement, params);
        }
        
        return em;
    }
}
