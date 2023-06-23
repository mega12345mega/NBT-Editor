package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;

public interface MultiVersionScreenParent {
	public default void renderBackground(MatrixStack matrices) {
		MultiVersionMisc.renderBackground((Screen) this, matrices);
	}
}
