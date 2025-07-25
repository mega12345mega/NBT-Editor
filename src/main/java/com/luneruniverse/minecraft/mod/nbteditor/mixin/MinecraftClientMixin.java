package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.async.UpdateCheckerThread;
import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
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
		if (screen == null)
			NBTEditorClient.CURSOR_MANAGER.onNoScreenSet();
		else if (screen instanceof HandledScreen<?> handledScreen)
			NBTEditorClient.CURSOR_MANAGER.onHandledScreenSet(handledScreen);
	}
	
	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/thread/ReentrantThreadExecutor;<init>(Ljava/lang/String;)V", shift = At.Shift.AFTER))
	private void init(RunArgs args, CallbackInfo info) {
		MixinLink.MAIN_THREAD = Thread.currentThread();
	}
	
	@Inject(method = "stop", at = @At("HEAD"))
	private void stop(CallbackInfo info) {
		if (NBTEditorClient.CLIENT_CHEST != null)
			NBTEditorClient.CLIENT_CHEST.stop();
	}
	
}
