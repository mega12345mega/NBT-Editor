package com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences;

import net.minecraft.client.gui.screen.ingame.HandledScreen;

public interface HandledScreenItemReference extends ItemReference {
	public void setParent(HandledScreen<?> screen);
	public default HandledScreenItemReference withParent(HandledScreen<?> screen) {
		setParent(screen);
		return this;
	}
}
