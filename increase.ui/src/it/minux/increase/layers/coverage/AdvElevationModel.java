package it.minux.increase.layers.coverage;

import it.minux.increase.ui.Config;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.terrain.BasicElevationModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;

public class AdvElevationModel 
	extends BasicElevationModel
	implements IAdvElevationModel
{

	private static final Log LOG = LogFactory.getLog(AdvElevationModel.class);	
	public AdvElevationModel(AVList params)
    {
        super(params);
    }

    public AdvElevationModel(Element domElement, AVList params)
    {
    	super(domElement, params);
    }

//    public AdvElevationModel(WMSCapabilities caps, AVList params)
//    {
//        super(caps, params);
//    }

    public AdvElevationModel(String restorableStateInXml)
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
//    	LOG.debug("elevation: " + els);
    	
    }
}
