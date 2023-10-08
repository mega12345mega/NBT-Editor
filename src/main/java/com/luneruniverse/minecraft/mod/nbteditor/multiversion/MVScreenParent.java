package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;

public interface MVScreenParent {
	default void renderBackground(MatrixStack matrices) {
		MVDrawableHelper.renderBackground((Screen) this, matrices);
	}
}
