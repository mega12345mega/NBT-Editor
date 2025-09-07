package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import java.io.IOException;
import java.lang.invoke.MethodType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.shaders.MVShader2;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gl.ShaderLoader;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.resource.LifecycledResourceManagerImpl;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.resource.ResourceType;

@Mixin(GameRenderer.class)
public class GameRendererMixin_1_21_2 {
	private static final Class<?> ShaderProgramKey = Reflection.getClass("net.minecraft.class_10156");
	private static final Reflection.MethodInvoker ShaderLoader_preload =
			Reflection.getMethod(ShaderLoader.class, "method_62944", MethodType.methodType(void.class, ResourceFactory.class, ShaderProgramKey.arrayType()));
	@Inject(method = "preloadPrograms", at = @At("HEAD"))
	private void preloadPrograms(ResourceFactory factory, CallbackInfo info) {
		LifecycledResourceManager manager = new LifecycledResourceManagerImpl(ResourceType.CLIENT_RESOURCES,
				MainUtil.client.getResourcePackManager().createResourcePacks());
		try {
			ShaderLoader_preload.invokeThrowable(Exception.class, MainUtil.client.getShaderLoader(),
					manager, MVShader2.SHADER_PROGRAM_KEYS.toArray(Object[]::new));
		} catch (IOException | ShaderLoader.LoadException e) {
			throw new RuntimeException("Could not preload shaders for loading UI", e);
		} catch (Exception e) {
			if (e instanceof RuntimeException runtime)
				throw runtime;
			// Impossible
			throw new RuntimeException("Error invoking method", e);
		} finally {
			manager.close();
		}
	}
}
