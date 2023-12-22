package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.util.WeakHashMap;

import net.minecraft.client.gui.Element;

public interface MVElement extends Element {
	
	static final WeakHashMap<MVElement, Boolean> _focused = new WeakHashMap<>();
	static final WeakHashMap<MVElement, Boolean> _multiFocused = new WeakHashMap<>();
	@Deprecated(since = "Added 1.19.4, not supported in earlier versions")
	public default void setFocused(boolean focused) {
		_focused.put(this, focused);
	}
	@Deprecated(since = "Added 1.19.4, not supported in earlier versions")
	public default boolean isFocused() {
		return _focused.getOrDefault(this, false);
	}
	public default void onFocusChange(boolean focused) {
		_multiFocused.put(this, focused);
	}
	public default boolean isMultiFocused() {
		return _multiFocused.getOrDefault(this, false);
	}
	
	public default boolean method_25401(double mouseX, double mouseY, double amount) {
		return mouseScrolled(mouseX, mouseY, 0, amount);
	}
	public default boolean mouseScrolled(double mouseX, double mouseY, double xAmount, double yAmount) {
		return false;
	}
	
}
