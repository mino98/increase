package it.minux.increase.utils;

import it.minux.increase.data.Tower;

public class TowerFilters {

	public static final IFilter<Tower> WITH_PANELS = new IFilter<Tower>() {
		
		@Override
		public boolean accept(Tower t) {
			return t.hasPanels();
		}
	};
	
	public static final IFilter<Tower> WITHOUT_PANELS = new IFilter<Tower>() {
		
		@Override
		public boolean accept(Tower t) {
			return !t.hasPanels();
		}
	};
	
	
}
