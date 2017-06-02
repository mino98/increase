package it.minux.increase.layers.coverage;

import it.minux.increase.ui.Config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import gov.nasa.worldwind.terrain.WMSBasicElevationModel;

public class AdvWMSElevationModel 
	extends WMSBasicElevationModel
	implements IAdvElevationModel
{
	private static final Log LOG = LogFactory.getLog(AdvWMSElevationModel.class);
	
	public AdvWMSElevationModel(AVList params)
    {
        super(params);
    }

    public AdvWMSElevationModel(Element domElement, AVList params)
    {
    	super(domElement, params);
    }

    public AdvWMSElevationModel(WMSCapabilities caps, AVList params)
    {
        super(caps, params);
    }

    public AdvWMSElevationModel(String restorableStateInXml)
    {
        super(restorableStateInXml);
    }
    
//    private int requiredLevel = -1;
//    
//    /**
//     * Which level of DTM is required for compution
//     * @return
//     */
//    private int getRequiredLevel() {
//    	if (requiredLevel < 0) {
//    		requiredLevel = Config.INSTANCE.getIntValue(Config.COVERAGE_DTM_LEVEL, 9);
//    	}
//    	
//    	return requiredLevel;
//    }
    
    @Override
    public void loadMaxResolution(Sector sector, int level) {
    	Elevations els = getElevations(sector, this.getLevels(), level);
    	// TODO wait until requested tiles are up in memory
    	LOG.debug("elevation: " + els);
    	
    }
}
