package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt;

import java.lang.invoke.MethodType;
import java.util.function.Supplier;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

import net.minecraft.component.ComponentHolder;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.component.ComponentsAccess;

public interface MVComponentsAccess {
	
	static class Impl {
		public static final boolean MOVED = Version.<Boolean>newSwitch()
				.range("1.21.5", null, true)
				.range(null, "1.21.4", false)
				.get();
		
		public static final Supplier<Reflection.MethodInvoker> ComponentHolder_get =
				Reflection.getOptionalMethod(ComponentHolder.class, "method_57824", MethodType.methodType(Object.class, ComponentType.class));
		public static final Supplier<Reflection.MethodInvoker> ComponentHolder_getOrDefault =
				Reflection.getOptionalMethod(ComponentHolder.class, "method_57825", MethodType.methodType(Object.class, ComponentType.class, Object.class));
		
		public static final Supplier<Reflection.MethodInvoker> ComponentMap_get =
				Reflection.getOptionalMethod(ComponentMap.class, "method_57829", MethodType.methodType(Object.class, ComponentType.class));
		public static final Supplier<Reflection.MethodInvoker> ComponentMap_getOrDefault =
				Reflection.getOptionalMethod(ComponentMap.class, "method_57830", MethodType.methodType(Object.class, ComponentType.class, Object.class));
		
		// If invoke instruction is in Mixin it crashes
		public static <T> T nbte$get(Object source, ComponentType<? extends T> type) {
			return ((ComponentsAccess) source).get(type);
		}
		public static <T> T nbte$getOrDefault(Object source, ComponentType<? extends T> type, T fallback) {
			return ((ComponentsAccess) source).getOrDefault(type, fallback);
		}
	}
	
	static class Impl_ComponentMap {
		private static final Supplier<Reflection.MethodInvoker> ComponentHolder_get =
				Reflection.getOptionalMethod(ComponentHolder.class, "method_57824", MethodType.methodType(Object.class, ComponentType.class));
		public static <T> T nbte$get(Object source, ComponentType<? extends T> type) {
			if (Impl.MOVED)
				return ((ComponentsAccess) source).get(type);
			else
				return ComponentHolder_get.get().invoke(source, type);
		}
		
		private static final Supplier<Reflection.MethodInvoker> ComponentHolder_getOrDefault =
				Reflection.getOptionalMethod(ComponentHolder.class, "method_57825", MethodType.methodType(Object.class, ComponentType.class, Object.class));
		public static <T> T nbte$getOrDefault(Object source, ComponentType<? extends T> type, T fallback) {
			if (Impl.MOVED)
				return ((ComponentsAccess) source).getOrDefault(type, fallback);
			else
				return ComponentHolder_getOrDefault.get().invoke(source, type, fallback);
		}
	}
	
	public default <T> T nbte$get(ComponentType<? extends T> type) {
		throw new RuntimeException("Missing implementation for MVComponentHolderParent#get");
	}
	
	public default <T> T nbte$getOrDefault(ComponentType<? extends T> type, T fallback) {
		throw new RuntimeException("Missing implementation for MVComponentHolderParent#getOrDefault");
	}
	
}
