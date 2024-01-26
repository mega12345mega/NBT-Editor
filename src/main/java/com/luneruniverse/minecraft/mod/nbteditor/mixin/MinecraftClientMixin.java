package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.async.UpdateCheckerThread;
import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.gui.screen.SplashOverlay;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
	@Inject(method = "setOverlay", at = @At("HEAD"))
	private void setOverlay(Overlay overlay, CallbackInfo info) {
		if (((MinecraftClient) (Object) this).getOverlay() instanceof SplashOverlay && overlay == null && !MixinLink.CLIENT_LOADED) {
			MixinLink.CLIENT_LOADED = true;
			new UpdateCheckerThread().start();
		}
	}
}
