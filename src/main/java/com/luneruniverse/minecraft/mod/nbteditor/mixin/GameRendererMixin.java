package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.llamalad7.mixinextras.sugar.Local;
import com.luneruniverse.minecraft.mod.nbteditor.misc.Shaders;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.resource.ResourceFactory;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@ModifyVariable(method = "loadPrograms", at = @At("STORE"), ordinal = 1)
	private List<Pair<ShaderProgram, Consumer<ShaderProgram>>> loadPrograms(
			List<Pair<ShaderProgram, Consumer<ShaderProgram>>> fragShaders, @Local ResourceFactory factory) {
		try {
			fragShaders.add(Pair.of(new ShaderProgram(factory, "position_hsv", Shaders.POSITION_HSV_VERTEX),
					program -> Shaders.POSITION_HSV_PROGRAM = program));
		} catch (IOException e) {
			throw new RuntimeException("could not reload shaders", e);
		}
		return fragShaders;
	}
}
