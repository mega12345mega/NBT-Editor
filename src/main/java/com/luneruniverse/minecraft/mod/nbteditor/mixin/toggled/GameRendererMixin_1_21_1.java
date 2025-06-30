package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.luneruniverse.minecraft.mod.nbteditor.misc.Shaders;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVShaders.MVShaderProgram;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.resource.ResourceFactory;

@Mixin(GameRenderer.class)
public class GameRendererMixin_1_21_1 {
	@ModifyVariable(method = "method_34538(Lnet/minecraft/class_5912;)V", at = @At("STORE"), ordinal = 1, remap = false)
	@Group(name = "loadPrograms", min = 1)
	@SuppressWarnings("target")
	private List<Pair<ShaderProgram, Consumer<ShaderProgram>>> loadPrograms_ResourceFactory(
			List<Pair<ShaderProgram, Consumer<ShaderProgram>>> fragShaders) {
		loadPrograms(fragShaders);
		return fragShaders;
	}
	@ModifyVariable(method = "method_34538(Lnet/minecraft/class_3300;)V", at = @At("STORE"), ordinal = 1, remap = false)
	@Group(name = "loadPrograms", min = 1)
	@SuppressWarnings("target")
	private List<Pair<ShaderProgram, Consumer<ShaderProgram>>> loadPrograms_ResourceManager(
			List<Pair<ShaderProgram, Consumer<ShaderProgram>>> fragShaders) {
		loadPrograms(fragShaders);
		return fragShaders;
	}
	private void loadPrograms(List<Pair<ShaderProgram, Consumer<ShaderProgram>>> fragShaders) {
		try {
			for (MVShaderProgram shader : Shaders.SHADERS) {
				fragShaders.add(Pair.of(
						Reflection.newInstanceThrowable(IOException.class, ShaderProgram.class,
								new Class<?>[] {ResourceFactory.class, String.class, VertexFormat.class},
								MainUtil.client.getResourceManager(), shader.key.name(), shader.key.vertexFormat()),
						program -> shader.shader = program));
			}
		} catch (IOException e) {
			throw new RuntimeException("could not reload shaders", e);
		}
	}
}
