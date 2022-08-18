package com.luneruniverse.minecraft.mod.nbteditor.screens.configurable;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;

public interface Configurable<T extends Configurable<T>> extends Drawable, Element {
	public static final int PADDING = 8;
	public boolean isValueValid();
	public int getSpacingHeight();
	public default int getRenderHeight() {
		return getSpacingHeight();
	}
	public T clone(boolean defaults);
	
	@Override
	public default boolean isMouseOver(double mouseX, double mouseY) {
		return mouseY >= 0 && mouseY <= getSpacingHeight();
	}
}
