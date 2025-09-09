package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.manager;

import java.util.function.Consumer;

import net.minecraft.nbt.NbtCompound;

/**
 * Convenience interface to avoid <code>NBTManagers.ITEM.getNbt(item)</code>
 */
public interface IntegratedNBTManager {
	public default NbtCompound nbte$serialize(boolean requireSuccess) {
		throw new RuntimeException("Missing implementation for IntegratedNBTManager#nbte$serialize");
	}
	
	public default boolean nbte$hasNbt() {
		throw new RuntimeException("Missing implementation for IntegratedNBTManager#nbte$hasNbt");
	}
	public default NbtCompound nbte$getNbt() {
		throw new RuntimeException("Missing implementation for IntegratedNBTManager#nbte$getNbt");
	}
	public default NbtCompound nbte$getOrCreateNbt() {
		throw new RuntimeException("Missing implementation for IntegratedNBTManager#nbte$getOrCreateNbt");
	}
	public default void nbte$setNbt(NbtCompound nbt) {
		throw new RuntimeException("Missing implementation for IntegratedNBTManager#nbte$setNbt");
	}
	
	public default void nbte$modifyNbt(Consumer<NbtCompound> modifier) {
		NbtCompound nbt = nbte$getOrCreateNbt();
		modifier.accept(nbt);
		nbte$setNbt(nbt);
	}
	public default void nbte$modifySubNbt(String tag, Consumer<NbtCompound> modifier) {
		NbtCompound nbt = nbte$getOrCreateNbt();
		NbtCompound subNbt = nbt.nbte$getCompoundOrDefault(tag);
		modifier.accept(subNbt);
		nbt.put(tag, subNbt);
		nbte$setNbt(nbt);
	}
}
