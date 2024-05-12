package com.luneruniverse.minecraft.mod.nbteditor.containers;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalEntity;

import net.minecraft.item.ItemStack;

public interface EntityContainerIO {
	public default boolean isEntityReadable(LocalEntity entity) {
		return true;
	}
	public ItemStack[] readEntity(LocalEntity container);
	public void writeEntity(LocalEntity container, ItemStack[] contents);
}
