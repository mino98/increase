package it.minux.increase.layers;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.Renderable;
import it.minux.increase.data.PointEvent;
import it.minux.increase.ui.Config;
import it.minux.increase.ui.IncreaseConfig;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HeatmapLayer<T extends PointEvent>
	extends LazyLayer	
{
	private static final Log LOG = LogFactory.getLog(HeatmapLayer.class);
	private List<T> events = null;
	private Heatmap<T> heatmap = null;
	
	public HeatmapLayer(List<T> events) {
		this.events = events;
		setName("Coverage Demand");
		setPickEnabled(false);
	}
	
	@Override
	protected List<Renderable> doLoadData() {
		List<Renderable> result = new ArrayList<Renderable>(1);
		try {
//			heatmap = createHeatmap(events);
			result.add(getHeatmap());
		} catch (Exception e) {
			LOG.error("Failed to create heatmap");
		}
		
		return result;
	}
	
	public synchronized Heatmap<T> getHeatmap() {
		if (heatmap ==  null) {
			heatmap = createHeatmap(events);
			events = null; // don't need them anymore
		}
		
		return heatmap;
	}

	protected Heatmap<T> createHeatmap(List<T> events) {
		Sector sector = IncreaseConfig.INSTANCE.getSectorOfInteres();
		
		int latres = Config.INSTANCE.getIntValue(Config.HEATMAP_LATRES, 100);
		int lonres = Config.INSTANCE.getIntValue(Config.HEATMAP_LONRES, 100);
		
		Heatmap<T> heatmap = new Heatmap<T>(sector, latres, lonres) {
			/**
			 * Template method for some postprocessing
			 * @param width
			 * @param height
			 * @param values
			 */
			protected void preprocessValues(int width, int height, double[] values) {
				HeatmapLayer.this.preprocessValues(width, height, values);
			}
		};
		heatmap.initData(events);
		return heatmap;
	}
	
	/**
	 * Template method for some postprocessing, subclasses may override this
	 * @param width
	 * @param height
	 * @param values
	 */
	protected void preprocessValues(int width, int height, double[] values) {
		// 
	}
}
