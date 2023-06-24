package com.luneruniverse.minecraft.mod.nbteditor.multiversion.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVScreenParent;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(Screen.class)
public class ScreenMixin implements MVScreenParent {
	// Needed for some reason ...
	// Prevents crash in 1.17 that's trying to find this method
	@Override
	public void renderBackground(MatrixStack matrices) {
		MVScreenParent.super.renderBackground(matrices);
	}
}
