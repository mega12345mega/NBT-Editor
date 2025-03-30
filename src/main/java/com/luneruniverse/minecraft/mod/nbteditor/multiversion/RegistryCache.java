package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.lang.invoke.MethodType;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class RegistryCache {
	
	private static final Map<DynamicRegistryManager, RegistryCache> caches = Collections.synchronizedMap(new WeakHashMap<>());
	public static RegistryCache get(DynamicRegistryManager registryManager) {
		return caches.computeIfAbsent(registryManager, key -> new RegistryCache(registryManager, false));
	}
	
	private static final Supplier<Reflection.MethodInvoker> Registry_getEntry =
			Reflection.getOptionalMethod(Registry.class, "method_55841", MethodType.methodType(Optional.class, Identifier.class));
	/**
	 * @return May be null
	 */
	public static <T> RegistryEntry.Reference<T> convertManagerWithCache(RegistryEntry.Reference<T> ref) {
		RegistryCache cache = get(DynamicRegistryManagerHolder.getManager());
		
		@SuppressWarnings("unchecked")
		Registry<T> registry = (Registry<T>) cache.getRegistry(ref.registryKey().getRegistry()).orElse(null);
		if (registry == null)
			return null;
		
		return Version.<Optional<RegistryEntry.Reference<T>>>newSwitch()
				.range("1.21.2", null, () -> registry.getEntry(ref.registryKey().getValue()))
				.range(null, "1.21.1", () -> Registry_getEntry.get().invoke(registry, ref.registryKey().getValue()))
				.get()
				.orElse(null);
	}
	
	private static final Supplier<Reflection.MethodInvoker> Registry_getKey =
			Reflection.getOptionalMethod(Registry.class, "method_30517", MethodType.methodType(RegistryKey.class));
	private static final LoadingCache<Registry<?>, Boolean> staticRegistries = CacheBuilder.newBuilder().build(
			CacheLoader.from(registry -> {
				return Version.<Boolean>newSwitch()
						.range("1.21.2", null, () -> Registries.REGISTRIES.get(registry.getKey().getValue()) != null)
						.range(null, "1.21.1", () -> MVRegistry.REGISTRIES.get(((RegistryKey<?>) Registry_getKey.get().invoke(registry)).getValue()) != null)
						.get();
			}));
	public static boolean isRegistryStatic(Registry<?> registry) {
		return staticRegistries.getUnchecked(registry);
	}
	
	private final WeakReference<DynamicRegistryManager> registryManagerRef;
	@SuppressWarnings("unused") // Holds a strong reference
	private final DynamicRegistryManager registryManager;
	private final Map<Identifier, Optional<? extends Registry<?>>> cache;
	
	public RegistryCache(DynamicRegistryManager registryManager, boolean stronglyRef) {
		this.registryManagerRef = new WeakReference<>(registryManager);
		this.registryManager = (stronglyRef ? registryManager : null);
		this.cache = new ConcurrentHashMap<>();
	}
	public RegistryCache(DynamicRegistryManager registryManager) {
		this(registryManager, true);
	}
	
	private static final Supplier<Reflection.MethodInvoker> DynamicRegistryManager_getOptional =
			Reflection.getOptionalMethod(DynamicRegistryManager.class, "method_33310", MethodType.methodType(Optional.class, RegistryKey.class));
	public Optional<? extends Registry<?>> getRegistry(Identifier registryKey) {
		return cache.computeIfAbsent(registryKey, id -> {
			DynamicRegistryManager registryManager = registryManagerRef.get();
			if (registryManager == null)
				return Optional.empty();
			return Version.<Optional<? extends Registry<?>>>newSwitch()
					.range("1.21.2", null, () -> registryManager.getOptional(RegistryKey.ofRegistry(id)))
					.range(null, "1.21.1", () -> DynamicRegistryManager_getOptional.get().invoke(registryManager, RegistryKey.ofRegistry(id)))
					.get();
		});
	}
	
	@SuppressWarnings("unchecked")
	public <T> Optional<? extends Registry<T>> getRegistry(RegistryKey<Registry<T>> registryKey) {
		return (Optional<? extends Registry<T>>) getRegistry(registryKey.getValue());
	}
	
}
