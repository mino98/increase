package it.minux.increase.layers;

import it.minux.increase.data.InstallationFailure;
import it.minux.increase.data.UserLocation;

import java.util.List;

public class InstallationFailuresLayer
	extends HeatmapLayer<InstallationFailure>
{
	private final Heatmap<UserLocation> userLocationsHeatmap;
	
	public InstallationFailuresLayer(List<InstallationFailure> failures, Heatmap<UserLocation> userLocationsHeatmap) {
		super(failures);
		this.userLocationsHeatmap = userLocationsHeatmap;
		
		setName("Installation Failures"); // TODO i18n
		setPickEnabled(false);
	}
	
	
	@Override
	protected void preprocessValues(int width, int height, double[] values) {
		double[] normData = userLocationsHeatmap.getRawData();
		if (normData == null || normData.length != values.length) {
			throw new IllegalStateException("Heatmaps have different sizes");
		}
		
		for (int i = 0; i < values.length; ++i) {
			double norm = normData[i];
			if (norm == 0.0) {
				values[i] = 0.0;
			} else {
				values[i] /= norm;
			}
			
			if (values[i] != 0.0) {
				int x = 1;
				x++;
			}
		}
 	}
}
