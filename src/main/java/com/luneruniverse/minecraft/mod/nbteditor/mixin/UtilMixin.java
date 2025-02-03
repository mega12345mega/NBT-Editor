package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;

import net.minecraft.util.Util;

@Mixin(Util.class)
public class UtilMixin {
	@Inject(method = "error(Ljava/lang/String;Ljava/lang/Throwable;)V", at = @At("HEAD"), require = 0)
	private static void error(String message, Throwable throwable, CallbackInfo info) {
		if (MixinLink.hiddenExceptionHandlers.contains(Thread.currentThread()))
			throw new MixinLink.HiddenException(message, throwable);
	}
	@Inject(method = "error(Ljava/lang/String;)V", at = @At("HEAD"), require = 0)
	private static void error(String message, CallbackInfo info) {
		if (MixinLink.hiddenExceptionHandlers.contains(Thread.currentThread()))
			throw new MixinLink.HiddenException(message, new RuntimeException(message));
	}
}
