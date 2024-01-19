package com.luneruniverse.minecraft.mod.nbteditor.misc;

import java.util.function.Supplier;

import com.google.common.collect.ImmutableMap;

import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayer.MultiPhaseParameters;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormatElement;
import net.minecraft.client.render.VertexFormats;

public class Shaders {
	
	public static record MVShader(Supplier<ShaderProgram> shader, RenderLayer layer) {}
	
	public static VertexFormat POSITION_HSV_VERTEX = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder()
			.put("Position", VertexFormats.POSITION_ELEMENT)
			.put("UV0", VertexFormats.TEXTURE_ELEMENT)
			.put("UV2", VertexFormats.LIGHT_ELEMENT)
			.build());
	public static ShaderProgram POSITION_HSV_PROGRAM;
	public static final RenderLayer GUI_HSV = RenderLayer.of("gui_hsv", POSITION_HSV_VERTEX, VertexFormat.DrawMode.QUADS, 786432,
			MultiPhaseParameters.builder().program(new RenderPhase.ShaderProgram(() -> POSITION_HSV_PROGRAM))
					.transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
					.depthTest(RenderPhase.LEQUAL_DEPTH_TEST)
					.build(false));
	public static final MVShader POSITION_HSV = new MVShader(() -> POSITION_HSV_PROGRAM, GUI_HSV);
	
}
