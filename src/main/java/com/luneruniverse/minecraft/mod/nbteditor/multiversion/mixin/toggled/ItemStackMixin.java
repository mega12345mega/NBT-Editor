package com.luneruniverse.minecraft.mod.nbteditor.multiversion.mixin.toggled;

import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

import org.spongepowered.asm.mixin.Mixin;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVComponentType;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.MVItemStackParent;

import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;

@Mixin(ItemStack.class)
public class ItemStackMixin implements MVItemStackParent {
	@Override
	public boolean contains(MVComponentType<?> type) {
		return ((ItemStack) (Object) this).contains((ComponentType<?>) type.getInternalValue());
	}
	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(MVComponentType<T> type) {
		return ((ItemStack) (Object) this).get((ComponentType<T>) type.getInternalValue());
	}
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getOrDefault(MVComponentType<T> type, T fallback) {
		return ((ItemStack) (Object) this).getOrDefault((ComponentType<T>) type.getInternalValue(), fallback);
	}
	@SuppressWarnings("unchecked")
	@Override
	public <T> T set(MVComponentType<T> type, T value) {
		return ((ItemStack) (Object) this).set((ComponentType<T>) type.getInternalValue(), value);
	}
	@SuppressWarnings("unchecked")
	@Override
	public <T> T apply(MVComponentType<T> type, T defaultValue, UnaryOperator<T> applier) {
		return ((ItemStack) (Object) this).apply((ComponentType<T>) type.getInternalValue(), defaultValue, applier);
	}
	@SuppressWarnings("unchecked")
	@Override
	public <T, U> T apply(MVComponentType<T> type, T defaultValue, U change, BiFunction<T, U, T> applier) {
		return ((ItemStack) (Object) this).apply((ComponentType<T>) type.getInternalValue(), defaultValue, change, applier);
	}
	@SuppressWarnings("unchecked")
	@Override
	public <T> T remove(MVComponentType<T> type) {
		return ((ItemStack) (Object) this).remove((ComponentType<T>) type.getInternalValue());
	}
}
