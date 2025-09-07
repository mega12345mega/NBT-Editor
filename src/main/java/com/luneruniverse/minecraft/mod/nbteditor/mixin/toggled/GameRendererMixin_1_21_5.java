package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import java.util.function.BiFunction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.shaders.MVShader3;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.systems.GpuDevice;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.util.Identifier;

@Mixin(GameRenderer.class)
public class GameRendererMixin_1_21_5 {
	@Inject(method = "preloadPrograms", at = @At("RETURN"))
	private void preloadPrograms(ResourceFactory factory, CallbackInfo info, @Local GpuDevice gpuDevice, @Local BiFunction<Identifier, ShaderType, String> sourceRetriever) {
		for (RenderPipeline pipeline : MVShader3.RENDER_PIPELINES)
			gpuDevice.precompilePipeline(pipeline, sourceRetriever);
	}
}
