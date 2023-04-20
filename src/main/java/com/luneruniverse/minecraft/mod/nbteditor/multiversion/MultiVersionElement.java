package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.util.WeakHashMap;

import net.minecraft.client.gui.Element;

public interface MultiVersionElement extends Element {
	
	static final WeakHashMap<MultiVersionElement, Boolean> _focused = new WeakHashMap<>();
	
	@Deprecated(since = "Added 1.19.4, not supported in earlier versions")
	default void setFocused(boolean focused) {
		_focused.put(this, focused);
	}
	
	@Deprecated(since = "Added 1.19.4, not supported in earlier versions")
	default boolean isFocused() {
		return _focused.getOrDefault(this, false);
	}
	
}
