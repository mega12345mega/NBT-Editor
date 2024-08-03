package com.luneruniverse.minecraft.mod.nbteditor.containers;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalEntity;

import net.minecraft.item.ItemStack;

public interface EntityContainerIO {
	public static EntityContainerIO forNBTIO(NBTContainerIO io) {
		return (EntityContainerIO) new EntityTagContainerIO(io);
	}
	
	/**
	 * @param entity May be null, which should result in 0 if it is not possible to determine the max size
	 */
	public int getMaxEntitySize(LocalEntity entity);
	public default boolean isEntityReadable(LocalEntity entity) {
		return true;
	}
	public ItemStack[] readEntity(LocalEntity container);
	public int writeEntity(LocalEntity container, ItemStack[] contents);
}
