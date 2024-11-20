package com.luneruniverse.minecraft.mod.nbteditor.misc;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableMap;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayer.MultiPhaseParameters;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormatElement;
import net.minecraft.client.render.VertexFormats;

public class Shaders {
	
	public static record MVShader(Supplier<ShaderProgram> shader, RenderLayer layer) {}
	
	private static VertexFormatElement getElement(String oldElement, Supplier<VertexFormatElement> newElement) {
		return Version.<VertexFormatElement>newSwitch()
				.range("1.21.0", null, newElement)
				.range(null, "1.20.6", () -> Reflection.getField(VertexFormats.class, oldElement, "Lnet/minecraft/class_296;").get(null))
				.get();
	}
	
	public static final VertexFormatElement POSITION_ELEMENT = getElement("field_1587", () -> VertexFormatElement.POSITION);
	public static final VertexFormatElement TEXTURE_ELEMENT = getElement("field_1591", () -> VertexFormatElement.UV_0);
	public static final VertexFormatElement LIGHT_ELEMENT = getElement("field_20886", () -> VertexFormatElement.UV_2);
	
	private static VertexFormat createFormat(Consumer<ImmutableMap.Builder<String, VertexFormatElement>> builderConsumer) {
		ImmutableMap.Builder<String, VertexFormatElement> mapBuilder = ImmutableMap.builder();
		builderConsumer.accept(mapBuilder);
		ImmutableMap<String, VertexFormatElement> map = mapBuilder.build();
		
		return Version.<VertexFormat>newSwitch()
				.range("1.21.0", null, () -> {
					VertexFormat.Builder vertexBuilder = VertexFormat.builder();
					map.forEach(vertexBuilder::add);
					return vertexBuilder.build();
				})
				.range(null, "1.20.6", () -> Reflection.newInstance(VertexFormat.class, new Class<?>[] {ImmutableMap.class}, map))
				.get();
	}
	
	public static VertexFormat POSITION_HSV_VERTEX = createFormat(builder -> builder
			.put("Position", POSITION_ELEMENT)
			.put("UV0", TEXTURE_ELEMENT)
			.put("UV2", LIGHT_ELEMENT));
	public static ShaderProgram POSITION_HSV_PROGRAM;
	public static final RenderLayer GUI_HSV = RenderLayer.of("gui_hsv", POSITION_HSV_VERTEX, VertexFormat.DrawMode.QUADS, 0xC0000,
			MultiPhaseParameters.builder().program(new RenderPhase.ShaderProgram(() -> POSITION_HSV_PROGRAM))
					.transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
					.depthTest(RenderPhase.LEQUAL_DEPTH_TEST)
					.build(false));
	public static final MVShader POSITION_HSV = new MVShader(() -> POSITION_HSV_PROGRAM, GUI_HSV);
	
}
