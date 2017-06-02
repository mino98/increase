package it.minux.increase.layers;

import it.minux.increase.data.CoverageRequest;

import java.util.List;

public class CoverageRequestsLayer
	extends HeatmapLayer<CoverageRequest>
{
	public CoverageRequestsLayer(List<CoverageRequest> requests) {
		super(requests);
		setName("Coverage Demand"); // TODO i18n
		setPickEnabled(false);
	}
}