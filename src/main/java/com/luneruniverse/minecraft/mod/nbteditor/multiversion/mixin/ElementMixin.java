package com.luneruniverse.minecraft.mod.nbteditor.multiversion.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVElementParent;

import net.minecraft.client.gui.Element;

@Mixin(Element.class)
public interface ElementMixin extends MVElementParent {
	// Needed for some reason ...
	// Prevents crash in 1.17 that's trying to find this method
	@Override
	default boolean mouseScrolled(double mouseX, double mouseY, double xAmount, double yAmount) {
		return MVElementParent.super.mouseScrolled(mouseX, mouseY, xAmount, yAmount);
	}
}
