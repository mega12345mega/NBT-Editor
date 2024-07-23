package com.luneruniverse.minecraft.mod.nbteditor.multiversion.mixin.toggled;

import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

import org.spongepowered.asm.mixin.Mixin;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDataComponentType;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.MVItemStackParent;

import net.minecraft.component.DataComponentType;
import net.minecraft.item.ItemStack;

@Mixin(ItemStack.class)
public class ItemStackMixin implements MVItemStackParent {
	@Override
	public boolean contains(MVDataComponentType<?> type) {
		return ((ItemStack) (Object) this).contains((DataComponentType<?>) type.getInternalValue());
	}
	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(MVDataComponentType<T> type) {
		return ((ItemStack) (Object) this).get((DataComponentType<T>) type.getInternalValue());
	}
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getOrDefault(MVDataComponentType<T> type, T fallback) {
		return ((ItemStack) (Object) this).getOrDefault((DataComponentType<T>) type.getInternalValue(), fallback);
	}
	@SuppressWarnings("unchecked")
	@Override
	public <T> T set(MVDataComponentType<T> type, T value) {
		return ((ItemStack) (Object) this).set((DataComponentType<T>) type.getInternalValue(), value);
	}
	@SuppressWarnings("unchecked")
	@Override
	public <T> T apply(MVDataComponentType<T> type, T defaultValue, UnaryOperator<T> applier) {
		return ((ItemStack) (Object) this).apply((DataComponentType<T>) type.getInternalValue(), defaultValue, applier);
	}
	@SuppressWarnings("unchecked")
	@Override
	public <T, U> T apply(MVDataComponentType<T> type, T defaultValue, U change, BiFunction<T, U, T> applier) {
		return ((ItemStack) (Object) this).apply((DataComponentType<T>) type.getInternalValue(), defaultValue, change, applier);
	}
	@SuppressWarnings("unchecked")
	@Override
	public <T> T remove(MVDataComponentType<T> type) {
		return ((ItemStack) (Object) this).remove((DataComponentType<T>) type.getInternalValue());
	}
}
