package com.luneruniverse.minecraft.mod.nbteditor.containers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public interface NBTContainerIO {
	public default boolean passRootNbt(SourceContainerType source) {
		return false;
	}
	/**
	 * @param nbt May be null, which should result in 0 if it is not possible to determine the max size
	 */
	public int getMaxNBTSize(NbtCompound nbt, SourceContainerType source);
	public default boolean isNBTReadable(NbtCompound nbt, SourceContainerType source) {
		return true;
	}
	public ItemStack[] readNBT(NbtCompound container, SourceContainerType source);
	public int writeNBT(NbtCompound container, ItemStack[] contents, SourceContainerType source);
}
