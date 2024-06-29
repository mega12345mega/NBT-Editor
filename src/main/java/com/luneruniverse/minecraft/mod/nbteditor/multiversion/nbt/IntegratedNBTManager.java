package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt;

import java.util.function.Consumer;

import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.TagReference;

import net.minecraft.nbt.NbtCompound;

/**
 * Convenience interface to avoid <code>NBTManagers.ITEM.getNbt(item)</code>
 */
public interface IntegratedNBTManager {
	public default NbtCompound manager$serialize() {
		throw new RuntimeException("Missing implementation for IntegratedNBTManager#manager$serialize");
	}
	
	public default boolean manager$hasNbt() {
		throw new RuntimeException("Missing implementation for IntegratedNBTManager#manager$hasNbt");
	}
	public default NbtCompound manager$getNbt() {
		throw new RuntimeException("Missing implementation for IntegratedNBTManager#manager$getNbt");
	}
	public default NbtCompound manager$getOrCreateNbt() {
		throw new RuntimeException("Missing implementation for IntegratedNBTManager#manager$getOrCreateNbt");
	}
	public default void manager$setNbt(NbtCompound nbt) {
		throw new RuntimeException("Missing implementation for IntegratedNBTManager#manager$setNbt");
	}
	
	public default void manager$modifyNbt(Consumer<NbtCompound> modifier) {
		NbtCompound nbt = manager$getOrCreateNbt();
		modifier.accept(nbt);
		manager$setNbt(nbt);
	}
	public default <T extends TagReference> void manager$modifyNbt(T tagRef, Consumer<T> modifier) {
		tagRef.load(manager$getOrCreateNbt());
		modifier.accept(tagRef);
		NbtCompound nbt = new NbtCompound();
		tagRef.save(nbt);
		manager$setNbt(nbt);
	}
	
	public default void manager$modifySubNbt(String tag, Consumer<NbtCompound> modifier) {
		NbtCompound nbt = manager$getOrCreateNbt();
		NbtCompound subNbt = nbt.getCompound(tag);
		modifier.accept(subNbt);
		nbt.put(tag, subNbt);
		manager$setNbt(nbt);
	}
	public default <T extends TagReference> void manager$modifySubNbt(String tag, T tagRef, Consumer<T> modifier) {
		NbtCompound nbt = manager$getOrCreateNbt();
		tagRef.load(nbt.getCompound(tag));
		modifier.accept(tagRef);
		NbtCompound subNbt = new NbtCompound();
		tagRef.save(subNbt);
		nbt.put(tag, subNbt);
		manager$setNbt(nbt);
	}
}
