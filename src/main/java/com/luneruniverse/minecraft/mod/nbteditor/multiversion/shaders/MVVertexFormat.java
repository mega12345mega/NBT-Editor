package com.luneruniverse.minecraft.mod.nbteditor.multiversion.shaders;

import java.lang.invoke.MethodType;
import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableMap;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

public class MVVertexFormat {
	
	private static final Supplier<Class<?>> net_minecraft_VertexFormat = Reflection.getOptionalClass("net.minecraft.class_293");
	private static final Supplier<Class<?>> VertexFormat$Builder = Reflection.getOptionalClass("net.minecraft.class_293$class_9803");
	private static final Supplier<Class<?>> VertexFormatElement = Reflection.getOptionalClass("net.minecraft.class_296");
	private static final Supplier<Reflection.MethodInvoker> VertexFormat_builder =
			Reflection.getOptionalMethod(net_minecraft_VertexFormat, () -> "method_60833", () -> MethodType.methodType(VertexFormat$Builder.get()));
	private static final Supplier<Reflection.MethodInvoker> VertexFormat$Builder_add =
			Reflection.getOptionalMethod(VertexFormat$Builder, () -> "method_60842", () -> MethodType.methodType(VertexFormat$Builder.get(), String.class, VertexFormatElement.get()));
	private static final Supplier<Reflection.MethodInvoker> VertexFormat$Builder_build =
			Reflection.getOptionalMethod(VertexFormat$Builder, () -> "method_60840", () -> MethodType.methodType(net_minecraft_VertexFormat.get()));
	public static MVVertexFormat of(Map<String, MVVertexFormatElement> elements) {
		return new MVVertexFormat(Version.<Object>newSwitch()
				.range("1.21.5", null, () -> MVVertexFormat_of.of(elements))
				.range("1.21.0", "1.21.4", () -> {
					Object vertexBuilder = VertexFormat_builder.get().invoke(null);
					elements.forEach((name, element) -> VertexFormat$Builder_add.get().invoke(vertexBuilder, name, element.getInternalValue()));
					return VertexFormat$Builder_build.get().invoke(vertexBuilder);
				})
				.range(null, "1.20.6", () -> {
					ImmutableMap.Builder<String, Object> mapBuilder = ImmutableMap.builder();
					elements.forEach((name, element) -> mapBuilder.put(name, element.getInternalValue()));
					ImmutableMap<String, Object> map = mapBuilder.build();
					return Reflection.newInstance(net_minecraft_VertexFormat.get(), new Class<?>[] {ImmutableMap.class}, map);
				})
				.get());
	}
	
	private final Object value;
	
	private MVVertexFormat(Object value) {
		this.value = value;
	}
	
	public Object getInternalValue() {
		return value;
	}
	
}
