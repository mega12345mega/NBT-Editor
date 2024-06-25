package com.luneruniverse.minecraft.mod.nbteditor.containers;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalBlock;

import net.minecraft.item.ItemStack;

public interface BlockContainerIO {
	public default boolean isBlockReadable(LocalBlock block) {
		return true;
	}
	public ItemStack[] readBlock(LocalBlock container);
	public void writeBlock(LocalBlock container, ItemStack[] contents);
}
