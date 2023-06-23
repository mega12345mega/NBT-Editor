package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.UncheckedExecutionException;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

public class Reflection {
	
	public static final MappingResolver mappings = FabricLoader.getInstance().getMappingResolver();
	
	
	private static final Cache<String, Class<?>> classCache = CacheBuilder.newBuilder().build();
	public static Class<?> getClass(String name) {
		try {
			return classCache.get(name, () -> {
				synchronized (mappings) {
					return Class.forName(mappings.mapClassName("intermediary", name));
				}
			});
		} catch (ExecutionException | UncheckedExecutionException e) {
			throw new RuntimeException("Error getting class", e);
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public static <T> T newInstance(Class<?> clazz, Class<?>[] parameters, Object... args) {
		try {
			return (T) clazz.getConstructor(parameters).newInstance(args);
		} catch (Exception e) {
			throw new RuntimeException("Error creating new instance of class", e);
		}
	}
	public static <T> T newInstance(String clazz, Class<?>[] parameters, Object... args) {
		return newInstance(getClass(clazz), parameters, args);
	}
	
	
	public static class FieldReference {
		private final Field field;
		public FieldReference(Field field) {
			this.field = field;
		}
		public void set(Object obj, Object value) {
			try {
				field.set(obj, value);
			} catch (Exception e) {
				throw new RuntimeException("Error setting field", e);
			}
		}
		@SuppressWarnings("unchecked")
		public <T> T get(Object obj) {
			try {
				return (T) field.get(obj);
			} catch (Exception e) {
				throw new RuntimeException("Error getting field value", e);
			}
		}
	}
	
	public static String getFieldName(Class<?> clazz, String field, String descriptor) {
		synchronized (mappings) {
			return mappings.mapFieldName("intermediary",
					mappings.unmapClassName("intermediary", clazz.getName()), field, descriptor);
		}
	}
	public static FieldReference getField(Class<?> clazz, String field, String descriptor) {
		try {
			return new FieldReference(clazz.getField(getFieldName(clazz, field, descriptor)));
		} catch (Exception e) {
			throw new RuntimeException("Error getting field", e);
		}
	}
	public static Supplier<FieldReference> getOptionalField(Class<?> clazz, String field, String descriptor) {
		return jitSupplier(() -> getField(clazz, field, descriptor));
	}
	
	
	public static class MethodInvoker {
		private final Method method;
		public MethodInvoker(Class<?> clazz, String method, MethodType type) throws Exception {
			this.method = clazz.getMethod(method, type.parameterArray());
			if (!type.returnType().isAssignableFrom(this.method.getReturnType())) {
				throw new NoSuchMethodException("Mismatched return types! Expected " + type.returnType().getName() +
						" but found " + this.method.getReturnType().getName());
			}
		}
		@SuppressWarnings("unchecked")
		public <T> T invoke(Object obj, Object... args) {
			try {
				return (T) method.invoke(obj, args);
			} catch (Exception e) {
				throw new RuntimeException("Error invoking method", e);
			}
		}
	}
	
	private static String getIntermediaryDescriptor(MethodType type) {
		StringBuilder output = new StringBuilder("(");
		for (Class<?> param : type.parameterArray())
			output.append(getIntermediaryDescriptor(param));
		output.append(")");
		output.append(getIntermediaryDescriptor(type.returnType()));
		return output.toString();
	}
	private static String getIntermediaryDescriptor(Class<?> clazz) {
		String descriptor = clazz.descriptorString();
		StringBuilder arrays = new StringBuilder();
		int typeStart = 0;
		while (descriptor.charAt(typeStart) == '[') {
			arrays.append('[');
			clazz = clazz.componentType();
			typeStart++;
		}
		if (descriptor.charAt(typeStart) == 'L') {
			synchronized (mappings) {
				return arrays + "L" + mappings.unmapClassName("intermediary", clazz.getName()).replace('.', '/') + ";";
			}
		} else
			return descriptor;
	}
	
	public static String getMethodName(Class<?> clazz, String method, MethodType type) {
		return mappings.mapMethodName("intermediary",
				mappings.unmapClassName("intermediary", clazz.getName()), method, getIntermediaryDescriptor(type));
	}
	public static MethodInvoker getMethod(Class<?> clazz, String method, MethodType type) {
		try {
			return new MethodInvoker(clazz, getMethodName(clazz, method, type), type);
		} catch (Exception e) {
			throw new RuntimeException("Error getting method", e);
		}
	}
	public static Supplier<MethodInvoker> getOptionalMethod(Supplier<Class<?>> clazz, Supplier<String> method, Supplier<MethodType> type) {
		return jitSupplier(() -> getMethod(clazz.get(), method.get(), type.get()));
	}
	public static Supplier<MethodInvoker> getOptionalMethod(Class<?> clazz, String method, MethodType type) {
		return getOptionalMethod(() -> clazz, () -> method, () -> type);
	}
	
	
	private static <T> Supplier<T> jitSupplier(Supplier<T> supplier) {
		return new Supplier<>() {
			private T value;
			@Override
			public T get() {
				if (value == null)
					value = supplier.get();
				return value;
			}
		};
	}
	
}
