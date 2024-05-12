package com.luneruniverse.minecraft.mod.nbteditor.containers;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalEntity;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class EntityTagContainerIO implements ItemContainerIO, EntityContainerIO {
	
	private final NBTContainerIO nbtIO;
	
	public EntityTagContainerIO(NBTContainerIO nbtIO) {
		this.nbtIO = nbtIO;
	}
	
	@Override
	public boolean isItemReadable(ItemStack item) {
		NbtCompound entityTag = item.getSubNbt("EntityTag");
		if (entityTag == null)
			entityTag = new NbtCompound();
		return nbtIO.isNBTReadable(entityTag);
	}
	@Override
	public ItemStack[] readItem(ItemStack container) {
		NbtCompound blockEntityTag = container.getSubNbt("EntityTag");
		if (blockEntityTag == null)
			blockEntityTag = new NbtCompound();
		return nbtIO.readNBT(blockEntityTag);
	}
	@Override
	public void writeItem(ItemStack container, ItemStack[] contents) {
		nbtIO.writeNBT(container.getOrCreateSubNbt("EntityTag"), contents);
	}
	
	@Override
	public boolean isEntityReadable(LocalEntity entity) {
		NbtCompound entityTag = entity.getNBT();
		if (entityTag == null)
			entityTag = new NbtCompound();
		return nbtIO.isNBTReadable(entityTag);
	}
	@Override
	public ItemStack[] readEntity(LocalEntity container) {
		NbtCompound entityTag = container.getNBT();
		if (entityTag == null)
			entityTag = new NbtCompound();
		return nbtIO.readNBT(entityTag);
	}
	@Override
	public void writeEntity(LocalEntity container, ItemStack[] contents) {
		nbtIO.writeNBT(container.getOrCreateNBT(), contents);
	}
	
}
