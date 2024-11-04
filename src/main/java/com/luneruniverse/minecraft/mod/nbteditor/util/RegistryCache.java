package com.luneruniverse.minecraft.mod.nbteditor.util;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.DynamicRegistryManagerHolder;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class RegistryCache {
	
	private static final Map<DynamicRegistryManager, RegistryCache> caches = Collections.synchronizedMap(new WeakHashMap<>());
	public static RegistryCache get(DynamicRegistryManager registryManager) {
		return caches.computeIfAbsent(registryManager, key -> new RegistryCache(registryManager, false));
	}
	
	/**
	 * @return May be null
	 */
	public static <T> RegistryEntry.Reference<T> convertManagerWithCache(RegistryEntry.Reference<T> ref) {
		RegistryCache cache = get(DynamicRegistryManagerHolder.getManager());
		
		@SuppressWarnings("unchecked")
		Registry<T> registry = (Registry<T>) cache.getRegistry(ref.registryKey().getRegistry()).orElse(null);
		if (registry == null)
			return null;
		
		return registry.getEntry(ref.registryKey().getValue()).orElse(null);
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
	
	public Optional<? extends Registry<?>> getRegistry(Identifier registryKey) {
		return cache.computeIfAbsent(registryKey, id -> {
			DynamicRegistryManager registryManager = registryManagerRef.get();
			if (registryManager == null)
				return Optional.empty();
			return registryManager.getOptional(RegistryKey.ofRegistry(id));
		});
	}
	
	@SuppressWarnings("unchecked")
	public <T> Optional<? extends Registry<T>> getRegistry(RegistryKey<Registry<T>> registryKey) {
		return (Optional<? extends Registry<T>>) getRegistry(registryKey.getValue());
	}
	
}
