package com.luneruniverse.minecraft.mod.nbteditor.multiversion.mixin.toggled;

import org.spongepowered.asm.mixin.Mixin;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.MVComponentsAccess;

import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;

@Mixin(ComponentMap.class)
public interface ComponentMapMixin extends MVComponentsAccess {
	
	@Override
	public default <T> T nbte$get(ComponentType<? extends T> type) {
		if (Impl.MOVED)
			return Impl.nbte$get(this, type);
		else
			return Impl.ComponentMap_get.get().invoke(this, type);
	}
	
	@Override
	public default <T> T nbte$getOrDefault(ComponentType<? extends T> type, T fallback) {
		if (Impl.MOVED)
			return Impl.nbte$getOrDefault(this, type, fallback);
		else
			return Impl.ComponentMap_getOrDefault.get().invoke(this, type, fallback);
	}
	
}
