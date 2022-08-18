package com.luneruniverse.minecraft.mod.nbteditor.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class OrderedMap<K, V> implements Map<K, V> {
	
	private final List<Entry<K, V>> internalMap;
	private Comparator<K> sorter;
	
	public OrderedMap(Comparator<K> sorter) {
		internalMap = new ArrayList<>();
		this.sorter = sorter;
	}
	public OrderedMap() {
		this(null);
	}
	
	public void sort(Comparator<K> sorter) {
		if (sorter == null)
			return;
		this.sorter = null;
		sortUnchecked(sorter);
	}
	private void sortUnchecked(Comparator<K> sorter) {
		if (sorter != null)
			internalMap.sort((a, b) -> sorter.compare(a.getKey(), b.getKey()));
	}
	public void setSorter(Comparator<K> sorter) {
		this.sorter = sorter;
		sort(sorter);
	}
	public Comparator<K> getSorter() {
		return sorter;
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
		return internalMap.stream().anyMatch(entry -> entry.getKey().equals(key));
	}
	
	@Override
	public boolean containsValue(Object value) {
		return internalMap.stream().anyMatch(entry -> entry.getValue().equals(value));
	}
	
	@Override
	public V get(Object key) {
		return internalMap.stream().filter(entry -> entry.getKey().equals(key)).findFirst().map(Entry::getValue).orElse(null);
	}
	
	@Override
	public V put(K key, V value) {
		V output = unsortedPut(key, value);
		sortUnchecked(sorter);
		return output;
	}
	private V unsortedPut(K key, V value) {
		return internalMap.stream().filter(entry -> entry.getKey().equals(key)).findFirst().map(entry -> entry.setValue(value)).orElseGet(() -> {
			internalMap.add(new Entry<>() {
				private V entryValue = value;
				@Override
				public K getKey() {
					return key;
				}
				@Override
				public V getValue() {
					return entryValue;
				}
				@Override
				public V setValue(V value) {
					V prev = entryValue;
					entryValue = value;
					return prev;
				}
			});
			return null;
		});
	}
	
	@Override
	public V remove(Object key) {
		for (Iterator<Entry<K, V>> i = internalMap.iterator(); i.hasNext();) {
			Entry<K, V> entry = i.next();
			if (entry.getKey().equals(key)) {
				i.remove();
				return entry.getValue();
			}
		}
		return null;
	}
	
	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		m.forEach(this::unsortedPut);
		sortUnchecked(sorter);
	}
	
	@Override
	public void clear() {
		internalMap.clear();
	}
	
	@Override
	public Set<K> keySet() {
		return new SetView<>(internalMap.stream().map(Entry::getKey).collect(Collectors.toList()));
	}
	
	@Override
	public Collection<V> values() {
		return new CollectionView<>(internalMap.stream().map(Entry::getValue).collect(Collectors.toList()));
	}
	
	@Override
	public Set<Entry<K, V>> entrySet() {
		return new SetView<>(new ArrayList<>(internalMap));
	}
	
	private class CollectionView<T> implements Collection<T> {
		
		private final List<T> data;
		
		public CollectionView(List<T> data) {
			this.data = data;
		}
		
		@Override
		public int size() {
			return data.size();
		}
		
		@Override
		public boolean isEmpty() {
			return data.isEmpty();
		}
		
		@Override
		public boolean contains(Object o) {
			return data.contains(o);
		}
		
		@Override
		public Iterator<T> iterator() {
			return new Iterator<>() {
				private int index = 0;
				@Override
				public boolean hasNext() {
					return index < data.size();
				}
				@Override
				public T next() {
					return data.get(index++);
				}
				@Override
				public void remove() {
					CollectionView.this.remove(data.get(--index));
				}
			};
		}
		
		@Override
		public Object[] toArray() {
			return data.toArray();
		}
		
		@Override
		public <T2> T2[] toArray(T2[] a) {
			return data.toArray(a);
		}
		
		@Override
		public boolean add(T e) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public boolean remove(Object o) {
			int i = data.indexOf(o);
			if (i == -1)
				return false;
			data.remove(i);
			internalMap.remove(i);
			return true;
		}
		
		@Override
		public boolean containsAll(Collection<?> c) {
			return data.containsAll(c);
		}
		
		@Override
		public boolean addAll(Collection<? extends T> c) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public boolean removeAll(Collection<?> c) {
			boolean changed = false;
			for (Object obj : c) {
				if (remove(obj))
					changed = true;
			}
			return changed;
		}
		
		@Override
		public boolean retainAll(Collection<?> c) {
			boolean changed = false;
			for (T entry : data) {
				if (!c.contains(entry)) {
					remove(entry);
					changed = true;
				}
			}
			return changed;
		}
		
		@Override
		public void clear() {
			data.clear();
			internalMap.clear();
		}
		
	}
	
	private class SetView<T> extends CollectionView<T> implements Set<T> {
		public SetView(List<T> data) {
			super(data);
		}
	}
	
}
