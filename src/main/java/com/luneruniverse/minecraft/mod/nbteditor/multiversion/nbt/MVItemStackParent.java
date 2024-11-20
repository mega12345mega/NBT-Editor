package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt;

import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVComponentType;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public interface MVItemStackParent {
	public default boolean manager$hasCustomName() {
		throw new RuntimeException("Missing implementation for MVItemStackParent#manager$hasCustomName");
	}
	public default ItemStack manager$setCustomName(Text name) {
		throw new RuntimeException("Missing implementation for MVItemStackParent#manager$setCustomName");
	}
	
	public default boolean contains(MVComponentType<?> type) {
		throw new RuntimeException("Missing implementation for MVItemStackParent#contains");
	}
	public default <T> T get(MVComponentType<T> type) {
		throw new RuntimeException("Missing implementation for MVItemStackParent#get");
	}
	public default <T> T getOrDefault(MVComponentType<T> type, T fallback) {
		throw new RuntimeException("Missing implementation for MVItemStackParent#getOrDefault");
	}
	public default <T> T set(MVComponentType<T> type, T value) {
		throw new RuntimeException("Missing implementation for MVItemStackParent#set");
	}
	public default <T> T apply(MVComponentType<T> type, T defaultValue, UnaryOperator<T> applier) {
		throw new RuntimeException("Missing implementation for MVItemStackParent#apply");
	}
	public default <T, U> T apply(MVComponentType<T> type, T defaultValue, U change, BiFunction<T, U, T> applier) {
		throw new RuntimeException("Missing implementation for MVItemStackParent#apply");
	}
	public default <T> T remove(MVComponentType<T> type) {
		throw new RuntimeException("Missing implementation for MVItemStackParent#remove");
	}
}
