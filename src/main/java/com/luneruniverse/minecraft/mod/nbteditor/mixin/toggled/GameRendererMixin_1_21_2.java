package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import java.io.IOException;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.misc.Shaders;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gl.ShaderLoader;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.resource.LifecycledResourceManagerImpl;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.resource.ResourceType;

@Mixin(GameRenderer.class)
public class GameRendererMixin_1_21_2 {
	@Inject(method = "preloadPrograms", at = @At("HEAD"))
	private void preloadPrograms(ResourceFactory factory, CallbackInfo info) {
		LifecycledResourceManager manager = new LifecycledResourceManagerImpl(ResourceType.CLIENT_RESOURCES,
				MainUtil.client.getResourcePackManager().createResourcePacks());
		try {
			MainUtil.client.getShaderLoader().preload(manager, Shaders.SHADERS.stream()
					.map(shader -> (ShaderProgramKey) shader.key.mcKey()).toArray(ShaderProgramKey[]::new));
		} catch (IOException | ShaderLoader.LoadException e) {
			throw new RuntimeException("Could not preload shaders for loading UI", e);
		} finally {
			manager.close();
		}
	}
}
