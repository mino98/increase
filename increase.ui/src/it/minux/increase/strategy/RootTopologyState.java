package it.minux.increase.strategy;

import it.minux.increase.data.Tower;
import it.minux.increase.data.TowerSet;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RootTopologyState 
	extends AbstractTopologyState
	implements ITopologyState
{
	
	private static final Log LOG = LogFactory.getLog(RootTopologyState.class);
	
	private final TowerSet towersInTopology;
	
	public RootTopologyState(TowerSet towersInTopology) {
		super(1);
		this.towersInTopology = towersInTopology;
	}
	
	@Override
	public boolean contains(Tower t) {
		return towersInTopology.contains(t);
	}

	@Override
	protected Iterator<Tower> iterator() {
		return towersInTopology.iterator();
	}
}
