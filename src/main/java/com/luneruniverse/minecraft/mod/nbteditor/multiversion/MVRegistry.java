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

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class MVRegistry<T> implements Iterable<T> {
	
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
	private static final Class<?> REGISTRIES_CLASS = Version.<Class<?>>newSwitch()
			.range("1.19.3", null, () -> Reflection.getClass("net.minecraft.class_7923"))
			.range(null, "1.19.2", () -> REGISTRY_CLASS)
			.get();
	private static <T> MVRegistry<T> getRegistry(String oldName, String newName, boolean defaulted) {
		return new MVRegistry<>(Reflection.getField(REGISTRIES_CLASS, Version.<String>newSwitch()
				.range("1.19.3", null, newName)
				.range(null, "1.19.2", oldName)
				.get(),
				defaulted ? Version.<String>newSwitch()
						.range("1.19.3", null, "Lnet/minecraft/class_7922;")
						.range(null, "1.19.2", "Lnet/minecraft/class_2348;")
						.get() : "Lnet/minecraft/class_2378;")
				.get(null));
	}
	
	public static final MVRegistry<? extends Registry<?>> REGISTRIES = getRegistry("field_11144", "field_41167", false);
	public static final MVRegistry<ScreenHandlerType<?>> SCREEN_HANDLER = getRegistry("field_17429", "field_41187", false);
	public static final MVRegistry<Item> ITEM = getRegistry("field_11142", "field_41178", true);
	public static final MVRegistry<Block> BLOCK = getRegistry("field_11146", "field_41175", true);
	public static final MVRegistry<EntityType<?>> ENTITY_TYPE = getRegistry("field_11145", "field_41177", true);
	public static final MVRegistry<Enchantment> ENCHANTMENT = getRegistry("field_11160", "field_41176", false);
	public static final MVRegistry<EntityAttribute> ATTRIBUTE = getRegistry("field_23781", "field_41190", false);
	public static final MVRegistry<Potion> POTION = getRegistry("field_11143", "field_41179", true);
	public static final MVRegistry<StatusEffect> STATUS_EFFECT = getRegistry("field_11159", "field_41174", false);
	
	public static <V, T extends V> T register(MVRegistry<V> registry, Identifier id, T entry) {
		return call(null, "method_10230", MethodType.methodType(Object.class, REGISTRY_CLASS, Identifier.class, Object.class), registry.value, id, entry);
	}
	
	
	private final Object value;
	
	private MVRegistry(Object value) {
		this.value = value;
	}
	
	@SuppressWarnings("unchecked")
	public Registry<T> getInternalValue() {
		return (Registry<T>) value;
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
