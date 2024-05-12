package com.luneruniverse.minecraft.mod.nbteditor.containers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public interface NBTContainerIO {
	public default boolean isNBTReadable(NbtCompound nbt) {
		return true;
	}
	public ItemStack[] readNBT(NbtCompound container);
	public void writeNBT(NbtCompound container, ItemStack[] contents);
}
