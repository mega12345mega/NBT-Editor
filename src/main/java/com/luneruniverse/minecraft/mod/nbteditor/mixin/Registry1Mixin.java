package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.DynamicRegistryManagerHolder;
import com.luneruniverse.minecraft.mod.nbteditor.util.RegistryCache;

import net.minecraft.registry.entry.RegistryEntry;

@Mixin(targets = "net.minecraft.registry.Registry$1")
public class Registry1Mixin {
	
	@ModifyVariable(method = "getRawId", at = @At("HEAD"))
	private RegistryEntry<?> getRawId(RegistryEntry<?> entry) {
		if (entry instanceof RegistryEntry.Reference<?> ref && DynamicRegistryManagerHolder.isOwnedByDefaultManager(ref)) {
			RegistryEntry.Reference<?> convertedRef = RegistryCache.convertManagerWithCache(ref);
			if (convertedRef != null)
				return convertedRef;
		}
		
		return entry;
	}
	
}
