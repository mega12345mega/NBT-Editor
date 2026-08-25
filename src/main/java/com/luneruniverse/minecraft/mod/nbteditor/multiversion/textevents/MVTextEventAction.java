package com.luneruniverse.minecraft.mod.nbteditor.multiversion.textevents;

import java.util.Optional;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

public interface MVTextEventAction<E, V> {
	public static final boolean EVENTS_REFACTORED = Version.<Boolean>newSwitch()
			.range("1.21.5", null, true)
			.range(null, "1.21.4", false)
			.get();
	
	public String getName();
	
	public Optional<V> parseValue(String valueStr);
	
	public V getValue(E event);
	public String getStringifiedValue(E event);
	
	public E newEvent(V value);
	public Optional<E> newEventWithParse(String valueStr);
}
