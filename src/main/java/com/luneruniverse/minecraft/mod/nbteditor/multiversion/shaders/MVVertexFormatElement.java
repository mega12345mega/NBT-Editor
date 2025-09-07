package com.luneruniverse.minecraft.mod.nbteditor.multiversion.shaders;

import java.util.function.Supplier;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.mojang.blaze3d.vertex.VertexFormatElement;

import net.minecraft.client.render.VertexFormats;

public class MVVertexFormatElement {
	
	private static MVVertexFormatElement getElement(String fieldName1, String fieldName2, Supplier<Object> field3) {
		return new MVVertexFormatElement(Version.<Object>newSwitch()
				.range("1.21.5", null, field3)
				.range("1.21.0", "1.21.4", () -> Reflection.getField(Reflection.getClass("net.minecraft.class_296"), fieldName2, "Lnet/minecraft/class_296;").get(null))
				.range(null, "1.20.6", () -> Reflection.getField(VertexFormats.class, fieldName1, "Lnet/minecraft/class_296;").get(null))
				.get());
	}
	
	public static final MVVertexFormatElement POSITION = getElement("field_1587", "field_52107", () -> VertexFormatElement.POSITION);
	public static final MVVertexFormatElement UV0 = getElement("field_1591", "field_52109", () -> VertexFormatElement.UV0);
	public static final MVVertexFormatElement UV2 = getElement("field_20886", "field_52112", () -> VertexFormatElement.UV2);
	
	private final Object value;
	
	private MVVertexFormatElement(Object value) {
		this.value = value;
	}
	
	public Object getInternalValue() {
		return value;
	}
	
}
