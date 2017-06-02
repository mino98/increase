package it.minux.increase.strategy;

import it.minux.increase.data.Tower;

import java.util.Iterator;

public class DerivedTopologyState 
	extends AbstractTopologyState
	implements ITopologyState
{
	
	private final AbstractTopologyState parentState;
	private final Tower tower;
	
	public DerivedTopologyState(int depth, AbstractTopologyState parentState, Tower tower) {
		super(depth);
		this.parentState = parentState;
		this.tower = tower;
	}

	@Override
	public boolean contains(Tower t) {
		if (this.equals(t)) {
			return true;
		}
		
		return parentState.contains(t);
	}
	
	private IStateEquivalenceKey eqKey = null;
	
	public IStateEquivalenceKey getEquivalenceKey()  {
		if (eqKey == null) {
			eqKey = createEquivalenceKey();
		}
		
		return eqKey;
	}
	
	private IStateEquivalenceKey createEquivalenceKey() {
		if (parentState instanceof DerivedTopologyState) {
			DerivedStateEquivalenceKey parentKey = (DerivedStateEquivalenceKey)(((DerivedTopologyState) parentState).getEquivalenceKey());
			return new DerivedStateEquivalenceKey(parentKey, tower.getId()); 
		} else {
			return new DerivedStateEquivalenceKey(tower.getId());
		}
	}

	@Override
	protected Iterator<Tower> iterator() {
		return new DelegatingIterator();
	}
	
	private class DelegatingIterator
		implements Iterator<Tower> {

		private boolean nexted = false;
		private Iterator<Tower> parentIt;
		
		@Override
		public boolean hasNext() {
			if (!nexted) {
				return true;
			}
		
			if (parentIt == null) {
				parentIt = parentState.iterator();
			}
 			
			return parentIt.hasNext();
		}

		@Override
		public Tower next() {
			if (!nexted) {
				nexted = true;
				return tower;
			}
			
			if (parentIt == null) {
				parentIt = parentState.iterator();
			}
 			
			return parentIt.next();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("remove()");
		}
	}
}
