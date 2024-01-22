package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.luneruniverse.minecraft.mod.nbteditor.misc.Shaders;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.GameRenderer;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@ModifyVariable(method = "loadPrograms", at = @At("STORE"), ordinal = 1)
	@Group(name = "loadPrograms", min = 1)
	private List<Pair<ShaderProgram, Consumer<ShaderProgram>>> loadPrograms_ResourceFactory(
			List<Pair<ShaderProgram, Consumer<ShaderProgram>>> fragShaders) {
		try {
			fragShaders.add(Pair.of(new ShaderProgram(MainUtil.client.getResourceManager(), "position_hsv", Shaders.POSITION_HSV_VERTEX),
					program -> Shaders.POSITION_HSV_PROGRAM = program));
		} catch (IOException e) {
			throw new RuntimeException("could not reload shaders", e);
		}
		return fragShaders;
	}
	@ModifyVariable(method = "method_34538(Lnet/minecraft/class_3300;)V", at = @At("STORE"), ordinal = 1, remap = false)
	@Group(name = "loadPrograms", min = 1)
	@SuppressWarnings("target")
	private List<Pair<ShaderProgram, Consumer<ShaderProgram>>> loadPrograms_ResourceManager(
			List<Pair<ShaderProgram, Consumer<ShaderProgram>>> fragShaders) {
		try {
			fragShaders.add(Pair.of(new ShaderProgram(MainUtil.client.getResourceManager(), "position_hsv", Shaders.POSITION_HSV_VERTEX),
					program -> Shaders.POSITION_HSV_PROGRAM = program));
		} catch (IOException e) {
			throw new RuntimeException("could not reload shaders", e);
		}
		return fragShaders;
	}
}
