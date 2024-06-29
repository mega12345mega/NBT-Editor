package com.luneruniverse.minecraft.mod.nbteditor.containers;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalEntity;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.TagNames;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class EntityTagContainerIO implements ItemContainerIO, EntityContainerIO {
	
	private final NBTContainerIO itemNbtIO;
	private final NBTContainerIO entityNbtIO;
	
	public EntityTagContainerIO(NBTContainerIO itemNbtIO, NBTContainerIO entityNbtIO) {
		this.itemNbtIO = itemNbtIO;
		this.entityNbtIO = entityNbtIO;
	}
	public EntityTagContainerIO(NBTContainerIO nbtIO) {
		this(nbtIO, nbtIO);
	}
	
	@Override
	public boolean isItemReadable(ItemStack item) {
		NbtCompound nbt = item.manager$getNbt();
		NbtCompound entityTag = (nbt == null ? new NbtCompound() : nbt.getCompound(TagNames.ENTITY_TAG));
		return itemNbtIO.isNBTReadable(entityTag, SourceContainerType.ITEM);
	}
	@Override
	public ItemStack[] readItem(ItemStack container) {
		NbtCompound nbt = container.manager$getNbt();
		NbtCompound entityTag = (nbt == null ? new NbtCompound() : nbt.getCompound(TagNames.ENTITY_TAG));
		return itemNbtIO.readNBT(entityTag, SourceContainerType.ITEM);
	}
	@Override
	public void writeItem(ItemStack container, ItemStack[] contents) {
		container.manager$modifySubNbt(TagNames.ENTITY_TAG,
				entityTag -> itemNbtIO.writeNBT(entityTag, contents, SourceContainerType.ITEM));
	}
	
	@Override
	public boolean isEntityReadable(LocalEntity entity) {
		NbtCompound entityTag = entity.getNBT();
		if (entityTag == null)
			entityTag = new NbtCompound();
		return entityNbtIO.isNBTReadable(entityTag, SourceContainerType.ENTITY);
	}
	@Override
	public ItemStack[] readEntity(LocalEntity container) {
		NbtCompound entityTag = container.getNBT();
		if (entityTag == null)
			entityTag = new NbtCompound();
		return entityNbtIO.readNBT(entityTag, SourceContainerType.ENTITY);
	}
	@Override
	public void writeEntity(LocalEntity container, ItemStack[] contents) {
		entityNbtIO.writeNBT(container.getOrCreateNBT(), contents, SourceContainerType.ENTITY);
	}
	
}
