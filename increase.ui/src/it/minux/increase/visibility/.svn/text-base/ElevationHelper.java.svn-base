package it.minux.increase.visibility;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.terrain.CompoundElevationModel;
import it.minux.increase.layers.coverage.IAdvElevationModel;
import it.minux.increase.ui.Config;

public class ElevationHelper {

	private static final Log LOG = LogFactory.getLog(ElevationHelper.class);
	
	public static final ElevationHelper INSTANCE = new ElevationHelper();
	
	 private final int requiredLevel;
	
	private ElevationHelper() {
		requiredLevel = Config.INSTANCE.getIntValue(Config.COVERAGE_DTM_LEVEL, 9);
	}
	
	public int getRequiredLevel() {
		return requiredLevel;
	}
	
	/**
	 * Ensure elevation model tiles are in memory before computing scanline
	 * @param y
	 */
	public void ensureElevationLoaded(Sector s, Globe globe) {
		IAdvElevationModel em = getAdvWMSElevationModel(globe);
		if (em == null) {
			LOG.error("Failed to find AdvWMSElevationModel, can't preload elevation tiles");
			return ;
		}
//		Sector s = computeScanlineBounds(y);
//		LOG.debug("preload: " + s);
		em.loadMaxResolution(s, ElevationHelper.INSTANCE.getRequiredLevel());
		
//		LOG.debug("em: " + em);
//		if (!(em instanceof ))
	}
	

	private IAdvElevationModel advElevationModel = null;
	
	private IAdvElevationModel getAdvWMSElevationModel(Globe globe) {
		if (advElevationModel == null) {
			ElevationModel em = globe.getElevationModel();
			if (em instanceof CompoundElevationModel) {
				for (ElevationModel emi : ((CompoundElevationModel)em).getElevationModels()) {
					if (emi instanceof IAdvElevationModel) {
						advElevationModel = (IAdvElevationModel)emi;
						break;
					}
				}
			} else if (em instanceof IAdvElevationModel) {
				advElevationModel = (IAdvElevationModel)em;
			}
		}
		
		return advElevationModel;
	}
}
