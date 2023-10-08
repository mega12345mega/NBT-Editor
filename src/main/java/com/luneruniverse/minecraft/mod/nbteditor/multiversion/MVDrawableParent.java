package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.util.math.MatrixStack;

public interface MVDrawableParent {
	default void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		MVDrawableHelper.render((Drawable) this, matrices, mouseX, mouseY, delta);
	}
}
