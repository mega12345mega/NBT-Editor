package com.luneruniverse.minecraft.mod.nbteditor.multiversion.shaders;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

public enum MVDrawMode {
	LINES,
	LINE_STRIP,
	DEBUG_LINES,
	DEBUG_LINE_STRIP,
	TRIANGLES,
	TRIANGLE_STRIP,
	TRIANGLE_FAN,
	QUADS;
	
	private final Object value;
	
	@SuppressWarnings("unchecked")
	private <T extends Enum<T>> MVDrawMode() {
		value = Enum.valueOf((Class<T>) Reflection.getClass(Version.<String>newSwitch()
				.range("1.21.5", null, "com.mojang.blaze3d.vertex.VertexFormat$class_5596")
				.range(null, "1.21.4", "net.minecraft.class_293$class_5596")
				.get()), name());
	}
	
	public Object getInternalValue() {
		return value;
	}
	
}
