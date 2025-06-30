package com.luneruniverse.minecraft.mod.nbteditor.containers;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalBlock;

import net.minecraft.item.ItemStack;

public interface BlockContainerIO {
	public static BlockContainerIO forNBTIO(NonItemNBTContainerIO io) {
		return (BlockContainerIO) new BlockEntityTagContainerIO(io.withItemSupport((String) null));
	}
	
	/**
	 * @param block May be null, which should result in 0 if it is not possible to determine the max size
	 */
	public int getMaxBlockSize(LocalBlock block);
	public default boolean isBlockReadable(LocalBlock block) {
		return true;
	}
	public ItemStack[] readBlock(LocalBlock container);
	public int writeBlock(LocalBlock container, ItemStack[] contents);
	public default int getWrittenBlockSlotIndex(LocalBlock container, ItemStack[] contents, int slot) {
		return slot;
	}
}
