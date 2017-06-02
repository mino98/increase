package it.minux.increase.data;

import it.minux.increase.utils.IFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TowerSet 
	implements Set<Tower>
{

	private final List<Tower> list;
	private final Map<String, Tower> index;
	
	public TowerSet(int size) {
		this.list = new ArrayList<Tower>(size);
		this.index = new HashMap<String, Tower>(size);
	}
	
	public TowerSet() {
		this.list = new ArrayList<Tower>();
		this.index = new HashMap<String, Tower>();
	}
	
	public void addTower(Tower t) {
		if (!this.index.containsKey(t.getId())) {
			this.index.put(t.getId(), t);
			this.list.add(t);
		}
	}

	@Override
	public Iterator<Tower> iterator() {
		return list.iterator();
	}
	
	public Tower getTowerById(String id) {
		return index.get(id);
	}

	@Override
	public boolean addAll(Collection<? extends Tower> arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains(Object arg0) {
		if (arg0 instanceof Tower) {
			return index.containsKey(((Tower)arg0).getId());
		}
		return false;
	}
	
	public boolean contains(String Id) {
		return index.containsKey(Id);
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		return list.containsAll(arg0);
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public boolean remove(Object arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T[] toArray(T[] arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean add(Tower e) {
		throw new UnsupportedOperationException();
	}
	
	public TowerSet subset(IFilter<Tower> filter) {
		TowerSet result = new TowerSet();
		
		for (Tower t : list) {
			if (filter.accept(t)) {
				result.addTower(t);
			}	
		}
		
		return result;
	}	
}
