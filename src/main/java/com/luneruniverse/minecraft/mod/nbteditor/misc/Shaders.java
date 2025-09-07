package com.luneruniverse.minecraft.mod.nbteditor.misc;

import java.util.Map;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.shaders.MVDrawMode;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.shaders.MVShader;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.shaders.MVVertexFormat;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.shaders.MVVertexFormatElement;

import net.minecraft.client.gl.RenderPipelines;

public class Shaders {
	
	public static MVVertexFormat POSITION_HSV_VERTEX_FORMAT = MVVertexFormat.of(Map.of(
			"Position", MVVertexFormatElement.POSITION,
			"UV0", MVVertexFormatElement.UV0,
			"UV2", MVVertexFormatElement.UV2));
	
	public static final MVShader POSITION_HSV = new MVShader.Builder(
			"position_hsv", "gui_hsv", POSITION_HSV_VERTEX_FORMAT, MVDrawMode.QUADS, 0xC0000, false)
			.withSnippet(() -> RenderPipelines.MATRICES_SNIPPET)
			.withTranslucentBlendFunc(true)
			.build();
	
}
