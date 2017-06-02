package it.minux.increase.layers;

import gov.nasa.worldwind.render.Renderable;
import it.minux.increase.ui.Config;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CombinedHeatmapLayer 
	extends LazyLayer
{
	private static final Log LOG = LogFactory.getLog(CombinedHeatmapLayer.class);
	
	private CombinedHeatmap heatmap = null;

	private final CoverageRequestsLayer layer1;
	private final SupportRequestsLayer layer2;
	private final InstallationFailuresLayer layer3;
	
	public CombinedHeatmapLayer(
			CoverageRequestsLayer layer1,
			SupportRequestsLayer layer2,
			InstallationFailuresLayer layer3) {
		setName("Combined Heatmap");
		setPickEnabled(false);
		
		this.layer1 = layer1;
		this.layer2 = layer2;
		this.layer3 = layer3;
	}
	
	@Override
	protected List<Renderable> doLoadData() {
		List<Renderable> result = new ArrayList<Renderable>(1);
		try {
//			heatmap = createHeatmap(events);
			result.add(getHeatmap());
		} catch (Exception e) {
			LOG.error("Failed to create combined heatmap");
		}
		
		return result;
	}
	
	public synchronized CombinedHeatmap getHeatmap() {
		if (heatmap ==  null) {
			heatmap = createHeatmap();
		}
		
		return heatmap;
	}

	private CombinedHeatmap createHeatmap() {
		double weightCoverage = 
			Config.INSTANCE.getFloatValue(Config.COMBINED_WEIGHT_COVERAGE, 0.33f);
		double weightSupport = 
			Config.INSTANCE.getFloatValue(Config.COMBINED_WEIGHT_SUPPORT, 0.33f);
		double weightInstallFailure = 
			Config.INSTANCE.getFloatValue(Config.COMBINED_WEIGHT_INSTALL, 0.33f);
		
		double coeffs[] = new double[] { weightCoverage, weightSupport, weightInstallFailure };
		
		CombinedHeatmap heatmap = new CombinedHeatmap(coeffs, 
				layer1.getHeatmap(), layer2.getHeatmap(), layer3.getHeatmap());
		
		return heatmap;
	}

}
