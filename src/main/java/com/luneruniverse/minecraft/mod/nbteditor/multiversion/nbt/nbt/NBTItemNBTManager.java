package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.nbt;

import java.lang.invoke.MethodType;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.Attempt;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.DeserializableNBTManager;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class NBTItemNBTManager implements DeserializableNBTManager<ItemStack> {
	
	private static final Reflection.MethodInvoker ItemStack_writeNbt =
			Reflection.getMethod(ItemStack.class, "method_7953", MethodType.methodType(NbtCompound.class, NbtCompound.class));
	@Override
	public Attempt<NbtCompound> trySerialize(ItemStack subject) {
		return new Attempt<>(ItemStack_writeNbt.invoke(subject, new NbtCompound()));
	}
	private static final Reflection.MethodInvoker ItemStack_fromNbt =
			Reflection.getMethod(ItemStack.class, "method_7915", MethodType.methodType(ItemStack.class, NbtCompound.class));
	@Override
	public Attempt<ItemStack> tryDeserialize(NbtCompound nbt) {
		return new Attempt<>(ItemStack_fromNbt.invoke(null, nbt));
	}
	
	private static final Reflection.MethodInvoker ItemStack_hasNbt =
			Reflection.getMethod(ItemStack.class, "method_7985", MethodType.methodType(boolean.class));
	@Override
	public boolean hasNbt(ItemStack subject) {
		return ItemStack_hasNbt.invoke(subject);
	}
	private static final Reflection.MethodInvoker ItemStack_getNbt =
			Reflection.getMethod(ItemStack.class, "method_7969", MethodType.methodType(NbtCompound.class));
	@Override
	public NbtCompound getNbt(ItemStack subject) {
		NbtCompound nbt = ItemStack_getNbt.invoke(subject);
		if (nbt == null)
			return null;
		return nbt.copy();
	}
	private static final Reflection.MethodInvoker ItemStack_getOrCreateNbt =
			Reflection.getMethod(ItemStack.class, "method_7948", MethodType.methodType(NbtCompound.class));
	@Override
	public NbtCompound getOrCreateNbt(ItemStack subject) {
		return ((NbtCompound) ItemStack_getOrCreateNbt.invoke(subject)).copy();
	}
	private static final Reflection.MethodInvoker ItemStack_setNbt =
			Reflection.getMethod(ItemStack.class, "method_7980", MethodType.methodType(void.class, NbtCompound.class));
	@Override
	public void setNbt(ItemStack subject, NbtCompound nbt) {
		ItemStack_setNbt.invoke(subject, nbt == null ? null : nbt.copy());
	}
	
}
