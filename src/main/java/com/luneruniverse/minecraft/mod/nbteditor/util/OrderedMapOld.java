package com.luneruniverse.minecraft.mod.nbteditor.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class OrderedMapOld<K, V> implements Map<K, V> {
	
	private class WeakMapKey {
		private final WeakReference<K> ref;
		private boolean added;
		private boolean containsKey;
		public WeakMapKey(K key) {
			this.ref = new WeakReference<>(key);
		}
		public void added() {
			this.added = true;
		}
		public K get() {
			K value = ref.get();
			if (value == null || (added && !keyExists(value)))
				return null;
			return value;
		}
		private boolean keyExists(K value) {
			if (containsKey)
				return true;
			try {
				containsKey = true;
				return internalMap.containsKey(value);
			} finally {
				containsKey = false;
			}
		}
	}
	
	private final List<WeakMapKey> orderedKeys;
	private final TreeMap<K, V> internalMap;
	private final Comparator<K> defaultSorter;
	private Comparator<K> sorter;
	
	public OrderedMapOld() {
		orderedKeys = new ArrayList<>();
		sorter = defaultSorter = (a, b) -> {
			for (Iterator<WeakMapKey> i = orderedKeys.iterator(); i.hasNext();) {
				K value = i.next().get();
				if (value == null)
					i.remove();
				else if (a.equals(value))
					return -1;
				else if (b.equals(value))
					return 1;
			}
			throw new IllegalStateException("Missing ordered keys!");
		};
		internalMap = new TreeMap<>((a, b) -> sorter.compare(a, b));
	}
	
	public void sort(Comparator<K> sorter) {
		orderedKeys.sort((a, b) -> {
			K aValue = a.get();
			K bValue = b.get();
			if (aValue == null)
				return bValue == null ? 0 : -1;
			if (bValue == null)
				return 1;
			return sorter.compare(aValue, bValue);
		});
		this.sorter = defaultSorter;
		resort();
	}
	public void setSorter(Comparator<K> sorter) {
		this.sorter = sorter;
		resort();
	}
	private void resort() {
		HashMap<K, V> entries = new HashMap<>(internalMap);
		internalMap.clear();
		internalMap.putAll(entries);
	}
	
	@Override
	public int size() {
		return internalMap.size();
	}
	
	@Override
	public boolean isEmpty() {
		return internalMap.isEmpty();
	}
	
	@Override
	public boolean containsKey(Object key) {
		return internalMap.containsKey(key);
	}
	
	@Override
	public boolean containsValue(Object value) {
		return internalMap.containsValue(value);
	}
	
	@Override
	public V get(Object key) {
		return internalMap.get(key);
	}
	
	@Override
	public V put(K key, V value) {
		WeakMapKey weakKey = null;
		try {
			if (!internalMap.containsKey(key))
				orderedKeys.add(weakKey = new WeakMapKey(key));
			return internalMap.put(key, value);
		} finally {
			if (weakKey != null)
				weakKey.added();
		}
	}
	
	@Override
	public V remove(Object key) {
		orderedKeys.removeIf(ref -> ref.get() == key || ref.get() == null);
		return internalMap.remove(key);
	}
	
	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		m.forEach(this::put);
	}
	
	@Override
	public void clear() {
		orderedKeys.clear();
		internalMap.clear();
	}
	
	@Override
	public Set<K> keySet() {
		return internalMap.keySet();
	}
	
	@Override
	public Collection<V> values() {
		return internalMap.values();
	}
	
	@Override
	public Set<Entry<K, V>> entrySet() {
		return internalMap.entrySet();
	}
	
}
