package com.luneruniverse.minecraft.mod.nbteditor.multiversion.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVScreen;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVScreenParent;

import net.minecraft.client.gui.DrawContext;
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
	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;renderBackground(Lnet/minecraft/client/gui/DrawContext;IIF)V"), require = 0)
	private void render_renderBackground(Screen screen, DrawContext context, int mouseX, int mouseY, float delta) {
		if (!((Object) this instanceof MVScreen))
			screen.renderBackground(context, mouseX, mouseY, delta);
		// Removes added renderBackground in 1.20.2+
	}
}
