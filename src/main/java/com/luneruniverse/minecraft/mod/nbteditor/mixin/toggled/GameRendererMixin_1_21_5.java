package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import java.io.IOException;
import java.io.Reader;
import java.util.function.BiFunction;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;
import com.luneruniverse.minecraft.mod.nbteditor.misc.Shaders;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.shaders.MVShader;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.shaders.MVShader3;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.systems.GpuDevice;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.resource.LifecycledResourceManagerImpl;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

@Mixin(GameRenderer.class)
public class GameRendererMixin_1_21_5 {
	@Shadow
	private static @Final Logger LOGGER;
	
	@Inject(method = "preloadPrograms", at = @At("RETURN"))
	private void preloadPrograms(ResourceFactory factory, CallbackInfo info, @Local GpuDevice gpuDevice) {
		try (LifecycledResourceManager manager = new LifecycledResourceManagerImpl(ResourceType.CLIENT_RESOURCES,
				MainUtil.client.getResourcePackManager().createResourcePacks())) {
			BiFunction<Identifier, ShaderType, String> sourceRetriever = (id, shaderType) -> {
				try (Reader reader = manager.getResourceOrThrow(shaderType.idConverter().toResourcePath(id)).getReader()) {
					return IOUtils.toString(reader);
				} catch (IOException e) {
					LOGGER.error("Coudln't preload {} shader {}: {}", shaderType, id, e);
					return null;
				}
			};
			
			for (MVShader shader : Shaders.SHADERS)
				gpuDevice.precompilePipeline(((MVShader3) shader).getPipeline(), sourceRetriever);
		}
	}
}
