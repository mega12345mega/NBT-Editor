package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.util.math.MatrixStack;

public interface MultiVersionDrawableParent {
	public default void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		MultiVersionDrawableHelper.render((Drawable) this, matrices, mouseX, mouseY, delta);
	}
}
