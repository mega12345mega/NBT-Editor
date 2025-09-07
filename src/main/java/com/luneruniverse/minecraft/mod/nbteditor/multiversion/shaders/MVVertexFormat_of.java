package com.luneruniverse.minecraft.mod.nbteditor.multiversion.shaders;

import java.util.Map;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

public class MVVertexFormat_of {
	
	// Avoid loading VertexFormat$Builder when MVVertexFormat is loaded by moving references to a different file
	public static Object of(Map<String, MVVertexFormatElement> elements) {
		VertexFormat.Builder vertexBuilder = VertexFormat.builder();
		elements.forEach((name, element) -> vertexBuilder.add(name, (VertexFormatElement) element.getInternalValue()));
		return vertexBuilder.build();
	}
	
}
