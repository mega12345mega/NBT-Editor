package com.luneruniverse.minecraft.mod.nbteditor.misc;

import java.util.ArrayList;
import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVShaders;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVShaders.MVShaderAndLayer;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVShaders.MVShaderProgram;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVShaders.MVShaderProgramKey;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayer.MultiPhaseParameters;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;

public class Shaders {
	
	public static final List<MVShaderProgram> SHADERS = new ArrayList<>();
	public static MVShaderProgram registerShader(MVShaderProgramKey key) {
		MVShaderProgram shader = new MVShaderProgram(key);
		SHADERS.add(shader);
		Version.newSwitch()
				.range("1.21.2", null, () -> ShaderProgramKeys.getAll().add((ShaderProgramKey) key.mcKey()))
				.range(null, "1.21.1", () -> {})
				.run();
		return shader;
	}
	
	public static VertexFormat POSITION_HSV_VERTEX = MVShaders.createFormat(builder -> builder
			.put("Position", MVShaders.POSITION_ELEMENT)
			.put("UV0", MVShaders.TEXTURE_ELEMENT)
			.put("UV2", MVShaders.LIGHT_ELEMENT));
	
	public static MVShaderProgramKey POSITION_HSV_PROGRAM_KEY = new MVShaderProgramKey("position_hsv", POSITION_HSV_VERTEX);
	public static MVShaderProgram POSITION_HSV_PROGRAM = registerShader(POSITION_HSV_PROGRAM_KEY);
	
	public static final RenderLayer GUI_HSV = RenderLayer.of("gui_hsv", POSITION_HSV_VERTEX, VertexFormat.DrawMode.QUADS, 0xC0000,
			MultiPhaseParameters.builder().program(MVShaders.newRenderPhaseShaderProgram(POSITION_HSV_PROGRAM))
					.transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
					.depthTest(RenderPhase.LEQUAL_DEPTH_TEST)
					.build(false));
	
	public static final MVShaderAndLayer POSITION_HSV = new MVShaderAndLayer(POSITION_HSV_PROGRAM, GUI_HSV);
	
}
