package com.luneruniverse.minecraft.mod.nbteditor.multiversion.registry;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryOwner;
import net.minecraft.registry.tag.TagKey;

public class DefaultRegistryEntry<T> extends RegistryEntry.Reference<T> {
	
	public static final Set<DefaultRegistryEntry<?>> INSTANCES = Collections.newSetFromMap(new ConcurrentHashMap<>());
	
	private RegistryEntry.Reference<T> convertedEntry;
	
	public DefaultRegistryEntry(RegistryEntryOwner<T> owner, RegistryKey<T> registryKey) {
		super(RegistryEntry.Reference.Type.STAND_ALONE, owner, registryKey, null);
		INSTANCES.add(this);
	}
	
	public void convert(DynamicRegistryManager clientManager) {
		if (clientManager == null)
			convertedEntry = null;
		else
			convertedEntry = RegistryCache.convertManager(this, clientManager);
	}
	
	private RegistryEntry.Reference<T> getConvertedEntry() {
		if (!DynamicRegistryManagerHolder.hasClientManager())
			return null;
		
		if (convertedEntry == null)
			throw new IllegalStateException("Client manager is missing standard entry: " + registryKey());
		
		return convertedEntry;
	}
	
	@Override
	public T value() {
		RegistryEntry.Reference<T> convertedEntry = getConvertedEntry();
		if (convertedEntry != null)
			return convertedEntry.value();
		
		return super.value();
	}
	
	@Override
	public boolean isIn(TagKey<T> tag) {
		RegistryEntry.Reference<T> convertedEntry = getConvertedEntry();
		if (convertedEntry != null)
			return convertedEntry.isIn(tag);
		
		return super.isIn(tag);
	}
	
	@Override
	public boolean ownerEquals(RegistryEntryOwner<T> owner) {
		return true;
	}
	
	@Override
	public boolean hasKeyAndValue() {
		RegistryEntry.Reference<T> convertedEntry = getConvertedEntry();
		if (convertedEntry != null)
			return convertedEntry.hasKeyAndValue();
		
		return super.hasKeyAndValue();
	}
	
	@Override
	public Stream<TagKey<T>> streamTags() {
		RegistryEntry.Reference<T> convertedEntry = getConvertedEntry();
		if (convertedEntry != null)
			return convertedEntry.streamTags();
		
		return super.streamTags();
	}
	
}
