package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import java.io.File;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;

import net.minecraft.client.texture.NativeImage;

@Mixin(NativeImage.class)
public class NativeImageMixin {
	@Inject(method = "writeTo(Ljava/io/File;)V", at = @At("HEAD"))
	private void writeTo(File file, CallbackInfo info) {
		MixinLink.screenshotTarget = file;
	}
}
