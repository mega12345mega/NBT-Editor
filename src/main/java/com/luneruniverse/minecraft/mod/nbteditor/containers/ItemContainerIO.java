package com.luneruniverse.minecraft.mod.nbteditor.containers;

import net.minecraft.item.ItemStack;

public interface ItemContainerIO {
	public static ItemContainerIO forNBTIO(NBTContainerIO io) {
		return new ItemTagContainerIO(null, io);
	}
	
	/**
	 * @param item May be null, which should result in 0 if it is not possible to determine the max size
	 */
	public int getMaxItemSize(ItemStack item);
	public default boolean isItemReadable(ItemStack item) {
		return true;
	}
	public ItemStack[] readItem(ItemStack container);
	public int writeItem(ItemStack container, ItemStack[] contents);
}
