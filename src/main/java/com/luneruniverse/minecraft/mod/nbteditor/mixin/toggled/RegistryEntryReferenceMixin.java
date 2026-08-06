package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.registry.DefaultRegistryEntry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.registry.DefaultRegistryManager;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryOwner;

@Mixin(RegistryEntry.Reference.class)
public abstract class RegistryEntryReferenceMixin<T> {
	
	@Inject(method = "<init>", at = @At("TAIL"))
	private void init(RegistryEntry.Reference.Type referenceType, RegistryEntryOwner<T> owner, RegistryKey<T> registryKey, T value, CallbackInfo info) {
		@SuppressWarnings("unchecked")
		RegistryEntry.Reference<T> source = (RegistryEntry.Reference<T>) (Object) this;
		
		if (DefaultRegistryManager.isOnLoadThread() || DefaultRegistryManager.isOwnedByOnlyDefaultManager(owner, registryKey)) {
			if (!(source instanceof DefaultRegistryEntry))
				throw new IllegalStateException("Must use DefaultRegistryEntry for default registry entries, not " + getClass().getName());
		}
	}
	
	@Inject(method = "standAlone", at = @At("HEAD"), cancellable = true)
	private static <T> void standAlone(RegistryEntryOwner<T> owner, RegistryKey<T> registryKey, CallbackInfoReturnable<RegistryEntry.Reference<T>> info) {
		if (DefaultRegistryManager.isOnLoadThread())
			info.setReturnValue(new DefaultRegistryEntry<>(owner, registryKey));
	}
	
	@Shadow
	public abstract RegistryKey<T> registryKey();
	
	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj))
			return true;
		
		if (obj instanceof RegistryEntry.Reference<?> ref &&
				((Object) this instanceof DefaultRegistryEntry || ref instanceof DefaultRegistryEntry)) {
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
