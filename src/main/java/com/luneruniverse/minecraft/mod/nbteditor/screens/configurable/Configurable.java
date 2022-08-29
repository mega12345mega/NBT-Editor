package com.luneruniverse.minecraft.mod.nbteditor.screens.configurable;

import java.util.WeakHashMap;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;

public interface Configurable<T extends Configurable<T>> extends Drawable, Element {
	public static final int PADDING = 8;
	public boolean isValueValid();
	public int getSpacingWidth();
	public int getSpacingHeight();
	public default int getRenderWidth() {
		return getSpacingWidth();
	}
	public default int getRenderHeight() {
		return getSpacingHeight();
	}
	public T clone(boolean defaults);
	
	public static final WeakHashMap<Configurable<?>, ConfigPath> PARENTS = new WeakHashMap<>();
	public default void setParent(ConfigPath parent) {
		PARENTS.put(this, parent);
	}
	public default ConfigPath getParent() {
		return PARENTS.get(this);
	}
	
	@Override
	public default boolean isMouseOver(double mouseX, double mouseY) {
		return mouseY >= 0 && mouseY <= getSpacingHeight();
	}
}
