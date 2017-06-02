package it.minux.increase.strategy;

import java.util.List;

import it.minux.increase.data.Tower;
import it.minux.increase.visibility.VisibilityGraph;

public interface ITopologyState {
	/**
	 * Run strategy search algorithm
	 */
	public List<DerivedTopologyState> expand(VisibilityGraph vg);
	
	/**
	 * Does this state contains given tower?
	 * @param t
	 * @return
	 */
	boolean contains(Tower t);
}
