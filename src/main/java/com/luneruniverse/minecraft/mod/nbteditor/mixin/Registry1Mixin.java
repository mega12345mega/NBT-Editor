package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import java.util.concurrent.atomic.AtomicReference;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.DynamicRegistryManagerHolder;

import net.minecraft.registry.entry.RegistryEntry;

@Mixin(targets = "net.minecraft.registry.Registry$1")
public class Registry1Mixin {
	
	@ModifyVariable(method = "getRawId", at = @At("HEAD"))
	private RegistryEntry<?> getRawId(RegistryEntry<?> entry) {
		AtomicReference<RegistryEntry<?>> output = new AtomicReference<>(entry);
		
		if (entry instanceof RegistryEntry.Reference<?> ref && DynamicRegistryManagerHolder.isOwnedByDefaultManager(ref)) {
			ref.getKey().ifPresent(key -> {
				DynamicRegistryManagerHolder.getManager().getOptional(key.getRegistryRef()).ifPresent(registry -> {
					registry.getEntry(key.getValue()).ifPresent(output::setPlain);
				});
			});
		}
		
		return output.getPlain();
	}
	
}
