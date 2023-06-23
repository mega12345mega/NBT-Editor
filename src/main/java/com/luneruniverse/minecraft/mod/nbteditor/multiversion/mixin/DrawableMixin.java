package com.luneruniverse.minecraft.mod.nbteditor.multiversion.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionDrawableParent;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(Drawable.class)
public interface DrawableMixin extends MultiVersionDrawableParent {
	// Needed for some reason ...
	// Prevents crash in 1.17 that's trying to find this method
	@Override
	default void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		MultiVersionDrawableParent.super.render(matrices, mouseX, mouseY, delta);
	}
}
