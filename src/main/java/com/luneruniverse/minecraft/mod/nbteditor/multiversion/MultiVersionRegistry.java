package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.lang.invoke.MethodType;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.UncheckedExecutionException;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class MultiVersionRegistry<T> implements Iterable<T> {
	
	// Wrapper handler
	private static final Cache<String, Reflection.MethodInvoker> methodCache = CacheBuilder.newBuilder().build();
	@SuppressWarnings("unchecked")
	private static <R> R call(Object registry, String method, MethodType type, Object... args) {
		try {
			return (R) methodCache.get(method, () -> Reflection.getMethod(Registry.class, method, type)).invoke(registry, args);
		} catch (ExecutionException | UncheckedExecutionException e) {
			throw new RuntimeException("Error invoking method", e);
		}
	}
	
	private static final Class<?> REGISTRY_CLASS = Reflection.getClass("net.minecraft.class_2378");
	private static final Class<?> REGISTRIES_CLASS = switch (Version.get()) {
		case v1_19_4, v1_19_3 -> Reflection.getClass("net.minecraft.class_7923");
		case v1_19, v1_18_v1_17 -> REGISTRY_CLASS;
	};
	private static <T> MultiVersionRegistry<T> getRegistry(String oldName, String newName, boolean defaulted) {
		return new MultiVersionRegistry<>(Reflection.getField(REGISTRIES_CLASS, switch (Version.get()) {
			case v1_19_4, v1_19_3 -> newName;
			case v1_19, v1_18_v1_17 -> oldName;
		}, defaulted ? switch (Version.get()) {
			case v1_19_4, v1_19_3 -> "Lnet/minecraft/class_7922;";
			case v1_19, v1_18_v1_17 -> "Lnet/minecraft/class_2348;";
		} : "Lnet/minecraft/class_2378;").get(null));
	}
	
	public static final MultiVersionRegistry<ScreenHandlerType<?>> SCREEN_HANDLER = getRegistry("field_17429", "field_41187", false);
	public static final MultiVersionRegistry<Item> ITEM = getRegistry("field_11142", "field_41178", true);
	public static final MultiVersionRegistry<Enchantment> ENCHANTMENT = getRegistry("field_11160", "field_41176", false);
	public static final MultiVersionRegistry<EntityAttribute> ATTRIBUTE = getRegistry("field_23781", "field_41190", false);
	public static final MultiVersionRegistry<Potion> POTION = getRegistry("field_11143", "field_41179", true);
	public static final MultiVersionRegistry<StatusEffect> STATUS_EFFECT = getRegistry("field_11159", "field_41174", false);
	
	public static <V, T extends V> T register(MultiVersionRegistry<V> registry, Identifier id, T entry) {
		return call(null, "method_10230", MethodType.methodType(Object.class, REGISTRY_CLASS, Identifier.class, Object.class), registry.value, id, entry);
	}
	
	
	private final Object value;
	
	private MultiVersionRegistry(Object value) {
		this.value = value;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Iterator<T> iterator() {
		return ((Iterable<T>) value).iterator();
	}
	
	public Optional<T> getOrEmpty(Identifier id) {
		return call(value, "method_17966", MethodType.methodType(Optional.class, Identifier.class), id);
	}
	
	public Identifier getId(T entry) {
		return call(value, "method_10221", MethodType.methodType(Identifier.class, Object.class), entry);
	}
	
	public T get(Identifier id) {
		return call(value, "method_10223", MethodType.methodType(Object.class, Identifier.class), id);
	}
	
	public Set<Identifier> getIds() {
		return call(value, "method_10235", MethodType.methodType(Set.class));
	}
	
	public Set<Map.Entry<Identifier, T>> getEntrySet() {
		Set<Map.Entry<Object, T>> output = call(value, "method_29722", MethodType.methodType(Set.class));
		return output.stream().map(entry -> Map.entry(getRegistryKeyValue(entry.getKey()), entry.getValue()))
				.collect(Collectors.toUnmodifiableSet());
	}
	private static Identifier getRegistryKeyValue(Object key) {
		return ((RegistryKey<?>) key).getValue();
	}
	
	public boolean containsId(Identifier id) {
		return call(value, "method_10250", MethodType.methodType(boolean.class, Identifier.class), id);
	}
	
}
