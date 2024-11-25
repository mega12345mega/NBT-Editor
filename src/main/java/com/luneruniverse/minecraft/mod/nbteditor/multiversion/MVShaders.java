package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableMap;

import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormatElement;
import net.minecraft.client.render.VertexFormats;

public class MVShaders {
	
	public static record MVShaderProgramKey(String name, VertexFormat vertexFormat, Object mcKey) {
		public MVShaderProgramKey(String name, VertexFormat vertexFormat) {
			this(name, vertexFormat, Version.newSwitch()
					.range("1.21.2", null, () -> new ShaderProgramKey(IdentifierInst.of("minecraft", "core/" + name), vertexFormat, Defines.EMPTY))
					.range(null, "1.21.1", () -> null)
					.get());
		}
	}
	public static class MVShaderProgram {
		public final MVShaderProgramKey key;
		public ShaderProgram shader;
		public MVShaderProgram(MVShaderProgramKey key) {
			this.key = key;
		}
	}
	public static record MVShaderAndLayer(MVShaderProgram shader, RenderLayer layer) {}
	
	private static VertexFormatElement getElement(String oldElement, Supplier<VertexFormatElement> newElement) {
		return Version.<VertexFormatElement>newSwitch()
				.range("1.21.0", null, newElement)
				.range(null, "1.20.6", () -> Reflection.getField(VertexFormats.class, oldElement, "Lnet/minecraft/class_296;").get(null))
				.get();
	}
	
	public static final VertexFormatElement POSITION_ELEMENT = getElement("field_1587", () -> VertexFormatElement.POSITION);
	public static final VertexFormatElement TEXTURE_ELEMENT = getElement("field_1591", () -> VertexFormatElement.UV_0);
	public static final VertexFormatElement LIGHT_ELEMENT = getElement("field_20886", () -> VertexFormatElement.UV_2);
	
	public static VertexFormat createFormat(Consumer<ImmutableMap.Builder<String, VertexFormatElement>> builderConsumer) {
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
	
	public static RenderPhase.ShaderProgram newRenderPhaseShaderProgram(MVShaderProgram shader) {
		return Version.<RenderPhase.ShaderProgram>newSwitch()
				.range("1.21.2", null, () -> new RenderPhase.ShaderProgram((ShaderProgramKey) shader.key.mcKey()))
				.range(null, "1.21.1", () -> Reflection.newInstance(RenderPhase.ShaderProgram.class, new Class<?>[] {Supplier.class}, (Supplier<ShaderProgram>) () -> shader.shader))
				.get();
	}
	
}
