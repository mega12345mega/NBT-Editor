package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.DynamicRegistryManagerHolder;
import com.luneruniverse.minecraft.mod.nbteditor.util.RegistryCache;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryOwner;
import net.minecraft.registry.tag.TagKey;

@Mixin(RegistryEntry.Reference.class)
public abstract class RegistryEntryReferenceMixin<T> {
	
	@Shadow
	public abstract RegistryKey<T> registryKey();
	
	@Inject(method = "value", at = @At("HEAD"), cancellable = true)
	private void value(CallbackInfoReturnable<T> info) {
		@SuppressWarnings("unchecked")
		RegistryEntry.Reference<T> source = (RegistryEntry.Reference<T>) (Object) this;
		
		if (DynamicRegistryManagerHolder.hasClientManager() && DynamicRegistryManagerHolder.isOwnedByDefaultManager(source)) {
			RegistryEntry.Reference<T> convertedRef = RegistryCache.convertManagerWithCache(source);
			if (convertedRef != null)
				info.setReturnValue(convertedRef.value());
		}
	}
	
	@Inject(method = "isIn", at = @At("HEAD"), cancellable = true)
	private void isIn(TagKey<T> tag, CallbackInfoReturnable<Boolean> info) {
		@SuppressWarnings("unchecked")
		RegistryEntry.Reference<T> source = (RegistryEntry.Reference<T>) (Object) this;
		
		if (DynamicRegistryManagerHolder.hasClientManager() && DynamicRegistryManagerHolder.isOwnedByDefaultManager(source)) {
			RegistryEntry.Reference<T> convertedRef = RegistryCache.convertManagerWithCache(source);
			if (convertedRef != null)
				info.setReturnValue(convertedRef.isIn(tag));
		}
	}
	
	@Inject(method = "ownerEquals", at = @At("RETURN"), cancellable = true)
	private void ownerEquals(RegistryEntryOwner<?> owner, CallbackInfoReturnable<Boolean> info) {
		if (!info.getReturnValueZ()) {
			if (DynamicRegistryManagerHolder.isOwnedByDefaultManager((RegistryEntry.Reference<?>) (Object) this))
				info.setReturnValue(true);
		}
	}
	
	@Inject(method = "streamTags", at = @At("HEAD"), cancellable = true)
	private void streamTags(CallbackInfoReturnable<Stream<TagKey<T>>> info) {
		@SuppressWarnings("unchecked")
		RegistryEntry.Reference<T> source = (RegistryEntry.Reference<T>) (Object) this;
		
		if (DynamicRegistryManagerHolder.hasClientManager() && DynamicRegistryManagerHolder.isOwnedByDefaultManager(source)) {
			RegistryEntry.Reference<T> convertedRef = RegistryCache.convertManagerWithCache(source);
			if (convertedRef != null)
				info.setReturnValue(convertedRef.streamTags());
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj))
			return true;
		
		if (obj instanceof RegistryEntry.Reference<?> ref &&
				(DynamicRegistryManagerHolder.isOwnedByDefaultManager((RegistryEntry.Reference<?>) (Object) this) ||
						DynamicRegistryManagerHolder.isOwnedByDefaultManager(ref))) {
			return registryKey().getRegistry().equals(ref.registryKey().getRegistry()) &&
					registryKey().getValue().equals(ref.registryKey().getValue());
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		return 31 * registryKey().getRegistry().hashCode() + registryKey().getValue().hashCode();
	}
	
}
