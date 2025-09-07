package com.luneruniverse.minecraft.mod.nbteditor.multiversion.shaders;

import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;

import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayer.MultiPhaseParameters;
import net.minecraft.client.render.RenderPhase;

public class MVShader1 extends MVShader {
	
	private static final Class<?> RenderPhase$ShaderProgram = Reflection.getClass("net.minecraft.class_4668$class_5942");
	
	private static final Class<?> RenderLayer$MultiPhase = Reflection.getClass("net.minecraft.class_1921$class_4687");
	private static final Class<?> VertexFormat = Reflection.getClass("net.minecraft.class_293");
	private static final Class<?> VertexFormat$DrawMode = Reflection.getClass("net.minecraft.class_293$class_5596");
	private static final Reflection.MethodInvoker RenderLayer_of =
			Reflection.getMethod(RenderLayer.class, "method_24048", MethodType.methodType(RenderLayer$MultiPhase, String.class, VertexFormat, VertexFormat$DrawMode, int.class, RenderLayer.MultiPhaseParameters.class));
	private static final Reflection.MethodInvoker RenderLayer$MultiPhaseParameters$Builder_program =
			Reflection.getMethod(RenderLayer.MultiPhaseParameters.Builder.class, "method_34578", MethodType.methodType(RenderLayer.MultiPhaseParameters.Builder.class, RenderPhase$ShaderProgram));
	private static final Reflection.MethodInvoker RenderLayer$MultiPhaseParameters$Builder_transparency =
			Reflection.getMethod(RenderLayer.MultiPhaseParameters.Builder.class, "method_23615", MethodType.methodType(RenderLayer.MultiPhaseParameters.Builder.class, Reflection.getClass("net.minecraft.class_4668$class_4685")));
	private static final Reflection.FieldReference RenderPhase_TRANSLUCENT_TRANSPARENCY =
			Reflection.getField(RenderPhase.class, "field_21370", "Lnet/minecraft/class_4668$class_4685;");
	static RenderLayer createLayer(MVShader.Builder builder, Object renderPhaseShaderProgram) {
		RenderLayer.MultiPhaseParameters.Builder paramsBuilder = MultiPhaseParameters.builder();
		RenderLayer$MultiPhaseParameters$Builder_program.invoke(paramsBuilder, renderPhaseShaderProgram);
		
		if (builder.isTranslucentBlendFunc()) {
			RenderLayer$MultiPhaseParameters$Builder_transparency.invoke(paramsBuilder,
					(Object) RenderPhase_TRANSLUCENT_TRANSPARENCY.get(null));
		}
		
		return RenderLayer_of.invoke(null,
				builder.getLayerName(),
				builder.getVertexFormat().getInternalValue(),
				builder.getDrawMode().getInternalValue(),
				builder.getExpectedBufferSize(),
				paramsBuilder.build(builder.isAffectsOutline()));
	}
	
	public static final List<MVShader1> SHADERS = new ArrayList<>();
	
	private final String shaderName;
	private final MVVertexFormat vertexFormat;
	private ShaderProgram shaderProgram;
	private final MVDrawMode drawMode;
	private final RenderLayer layer;
	
	public MVShader1(MVShader.Builder builder) {
		shaderName = builder.getShaderName();
		vertexFormat = builder.getVertexFormat();
		drawMode = builder.getDrawMode();
		
		Object renderPhaseShaderProgram = Reflection.newInstance(RenderPhase$ShaderProgram,
				new Class<?>[] {Supplier.class},
				(Supplier<ShaderProgram>) () -> shaderProgram);
		
		layer = createLayer(builder, renderPhaseShaderProgram);
		
		SHADERS.add(this);
	}
	
	public void setShaderProgram(ShaderProgram shaderProgram) {
		this.shaderProgram = shaderProgram;
	}
	
	public String getShaderName() {
		return shaderName;
	}
	
	public MVVertexFormat getVertexFormat() {
		return vertexFormat;
	}
	
	public ShaderProgram getShaderProgram() {
		return shaderProgram;
	}
	
	public MVDrawMode getDrawMode() {
		return drawMode;
	}
	
	@Override
	public RenderLayer getLayer() {
		return layer;
	}
	
}
