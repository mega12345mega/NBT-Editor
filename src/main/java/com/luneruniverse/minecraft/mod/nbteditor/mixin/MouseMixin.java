package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.Mouse;

@Mixin(Mouse.class)
public class MouseMixin {
	
	@Shadow
	private double x;
	@Shadow
	private double y;
	
	@Inject(method = "unlockCursor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;getHandle()J"))
	private void unlockCursor_setCursorParameters(CallbackInfo info) {
		if (MixinLink.shouldRestoreMousePosition()) {
			x = MixinLink.getRestoreMouseX();
			y = MixinLink.getRestoreMouseY();
		}
	}
	
	// Order of glfwSetCursorPos, glfwSetInputMode in InputUtil#setCursorParameters makes the first useless
	@Inject(method = "unlockCursor", at = @At(value = "TAIL"))
	private void unlockCursor_tail(CallbackInfo info) {
		GLFW.glfwSetCursorPos(MainUtil.client.getWindow().getHandle(), x, y);
	}
	
}
