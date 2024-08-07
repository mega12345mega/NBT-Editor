package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.async.UpdateCheckerThread;
import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.ClientHandledScreen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.client.gui.screen.ingame.HandledScreen;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
	@Inject(method = "setOverlay", at = @At("HEAD"))
	private void setOverlay(Overlay overlay, CallbackInfo info) {
		if (((MinecraftClient) (Object) this).getOverlay() instanceof SplashOverlay && overlay == null && !MixinLink.CLIENT_LOADED) {
			MixinLink.CLIENT_LOADED = true;
			new UpdateCheckerThread().start();
		}
	}
	@Inject(method = "setScreen", at = @At("HEAD"))
	private void setScreen(Screen screen, CallbackInfo info) {
		if (screen instanceof HandledScreen<?> handledScreen) {
			int syncId = handledScreen.getScreenHandler().syncId;
			if (syncId != 0 && syncId != ClientHandledScreen.SYNC_ID)
				MixinLink.LAST_SERVER_HANDLED_SCREEN = handledScreen;
		}
	}
}
