package com.luneruniverse.minecraft.mod.nbteditor.util;

import java.util.HashMap;
import java.util.Map;

public class ClassMap<K, V> {
	
	private final Map<Class<? extends K>, V> map;
	
	public ClassMap() {
		map = new HashMap<>();
	}
	
	public Class<? extends K> getActualKey(Class<? extends K> clazz) {
		for (Map.Entry<Class<? extends K>, V> entry : map.entrySet()) {
			if (entry.getKey().isAssignableFrom(clazz))
				return entry.getKey();
		}
		return clazz;
	}
	
	public V get(Class<? extends K> clazz) {
		return map.get(getActualKey(clazz));
	}
	
	public void put(Class<? extends K> clazz, V value) {
		map.put(clazz, value);
	}
	
}
