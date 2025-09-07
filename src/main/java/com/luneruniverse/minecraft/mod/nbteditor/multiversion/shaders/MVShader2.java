package com.luneruniverse.minecraft.mod.nbteditor.multiversion.shaders;

import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IdentifierInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;

import net.minecraft.client.gl.Defines;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

public class MVShader2 extends MVShader {
	
	public static final List<Object> SHADER_PROGRAM_KEYS = new ArrayList<>();
	
	private final RenderLayer layer;
	
	private static final Class<?> VertexFormat = Reflection.getClass("net.minecraft.class_293");
	private static final Class<?> ShaderProgramKey = Reflection.getClass("net.minecraft.class_10156");
	private static final Class<?> ShaderProgramKeys = Reflection.getClass("net.minecraft.class_10142");
	private static final Class<?> RenderPhase$ShaderProgram = Reflection.getClass("net.minecraft.class_4668$class_5942");
	private static final Reflection.MethodInvoker ShaderProgramKeys_getAll =
			Reflection.getMethod(ShaderProgramKeys, "method_62901", MethodType.methodType(List.class));
	public MVShader2(MVShader.Builder builder) {
		Object shaderProgramKey = Reflection.newInstance(ShaderProgramKey,
				new Class<?>[] {Identifier.class, VertexFormat, Defines.class},
				IdentifierInst.of("minecraft", "core/" + builder.getShaderName()), builder.getVertexFormat().getInternalValue(), Defines.EMPTY);
		Object renderPhaseShaderProgram = Reflection.newInstance(RenderPhase$ShaderProgram,
				new Class<?>[] {ShaderProgramKey},
				shaderProgramKey);
		
		layer = MVShader1.createLayer(builder, renderPhaseShaderProgram);
		
		ShaderProgramKeys_getAll.<List<Object>>invoke(null).add(shaderProgramKey);
		SHADER_PROGRAM_KEYS.add(shaderProgramKey);
	}
	
	@Override
	public RenderLayer getLayer() {
		return layer;
	}
	
}
