package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt;

import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDataComponentType;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public interface MVItemStackParent {
	public default boolean manager$hasCustomName() {
		throw new RuntimeException("Missing implementation for MVItemStackParent#manager$hasCustomName");
	}
	public default ItemStack manager$setCustomName(Text name) {
		throw new RuntimeException("Missing implementation for MVItemStackParent#manager$setCustomName");
	}
	
	public default boolean contains(MVDataComponentType<?> type) {
		throw new RuntimeException("Missing implementation for MVItemStackParent#contains");
	}
	public default <T> T get(MVDataComponentType<T> type) {
		throw new RuntimeException("Missing implementation for MVItemStackParent#get");
	}
	public default <T> T getOrDefault(MVDataComponentType<T> type, T fallback) {
		throw new RuntimeException("Missing implementation for MVItemStackParent#getOrDefault");
	}
	public default <T> T set(MVDataComponentType<T> type, T value) {
		throw new RuntimeException("Missing implementation for MVItemStackParent#set");
	}
	public default <T> T apply(MVDataComponentType<T> type, T defaultValue, UnaryOperator<T> applier) {
		throw new RuntimeException("Missing implementation for MVItemStackParent#apply");
	}
	public default <T, U> T apply(MVDataComponentType<T> type, T defaultValue, U change, BiFunction<T, U, T> applier) {
		throw new RuntimeException("Missing implementation for MVItemStackParent#apply");
	}
	public default <T> T remove(MVDataComponentType<T> type) {
		throw new RuntimeException("Missing implementation for MVItemStackParent#remove");
	}
}
