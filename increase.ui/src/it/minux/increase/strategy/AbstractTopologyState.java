package it.minux.increase.strategy;

import it.minux.increase.data.Tower;
import it.minux.increase.data.TowerSet;
import it.minux.increase.ui.Config;
import it.minux.increase.visibility.VisibilityGraph;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractTopologyState
	implements ITopologyState
{
//	private static final int LOOKAHEAD_STEPS = 
//		Config.INSTANCE.getIntValue(Config.LOOKAHEAD_STEPS, 5);
	
	private static final Log LOG = LogFactory.getLog(AbstractTopologyState.class);

	private final int depth;
	
	public AbstractTopologyState(int depth) {
		this.depth = depth;
	}
	
	@Override
	public List<DerivedTopologyState> expand(VisibilityGraph vg) {
//		if (depth > LOOKAHEAD_STEPS) {
//			return null;
//		}
		
		TowerSet expanded = new TowerSet();
		List<DerivedTopologyState> derivedStates = new LinkedList<DerivedTopologyState>(); 
		
		Iterator<Tower> it = iterator();
		while (it.hasNext()) {
			Tower t = it.next();
			List<Tower> directlyVisible = vg.getDirectlyVisibly(t);
			if (directlyVisible == null) {
				continue;
			}
			for (Tower m : directlyVisible) {
				if (!contains(m)) {
					expanded.addTower(m);
					DerivedTopologyState derivedState = 
						new DerivedTopologyState(depth + 1, this, m);
					derivedStates.add(derivedState);
				}
			}
		}
	
//		LOG.debug(expanded.size());
		
//		for (DerivedTopologyState state : derivedStates) {
//			state.expand(vg);
//		}
		return derivedStates;
	}

	protected abstract Iterator<Tower> iterator();
	
}
