package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.util.math.MatrixStack;

public interface MVDrawable extends Drawable {
	@Override
	public default void render(DrawContext context, int mouseX, int mouseY, float delta) {
		render(MVDrawableHelper.getMatrices(context), mouseX, mouseY, delta);
	}
	public default void method_25394(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		render(matrices, mouseX, mouseY, delta);
	}
	public void render(MatrixStack context, int mouseX, int mouseY, float delta);
}
