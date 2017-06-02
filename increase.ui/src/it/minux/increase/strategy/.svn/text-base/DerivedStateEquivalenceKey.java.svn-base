package it.minux.increase.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class DerivedStateEquivalenceKey implements IStateEquivalenceKey {

	private final String keyString;
	private final List<String> keys;
	
	public DerivedStateEquivalenceKey(String id) {
		if (id == null) {
			throw new IllegalArgumentException();
		}
		keyString = id;
		keys = new ArrayList<String>(1);
		keys.add(id);
	}
	
	
	public DerivedStateEquivalenceKey(DerivedStateEquivalenceKey parent, String id) {
		if (id == null) {
			throw new IllegalArgumentException();
		}
		List<String> newKeys = new LinkedList<String>();
		newKeys.addAll(parent.keys);
		newKeys.add(id);
		Collections.sort(newKeys);
		
		this.keys = newKeys;
		this.keyString = toKeyString(newKeys);
	}
	
	private String toKeyString(List<String> newKeys) {
		StringBuilder builder = new StringBuilder();
		for (String id : newKeys) {
			builder.append(id);
			builder.append(",");
		}
		return builder.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DerivedStateEquivalenceKey)) {
			return false;
		}
		
		DerivedStateEquivalenceKey that = (DerivedStateEquivalenceKey)obj;
		return this.keyString.equals(that.keyString);
	}
	
	@Override
	public int hashCode() {
		return this.keyString.hashCode();
	}
}
