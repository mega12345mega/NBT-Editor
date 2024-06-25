package com.luneruniverse.minecraft.mod.nbteditor.containers;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalEntity;

import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.NbtCompound;

public class SpawnEggContainerIO implements ItemContainerIO {
	
	@Override
	public boolean isItemReadable(ItemStack item) {
		NbtCompound entityTag = item.getSubNbt("EntityTag");
		if (entityTag == null)
			entityTag = new NbtCompound();
		return ContainerIO.isContainer(new LocalEntity(getEntityType(item), entityTag));
	}
	
	@Override
	public ItemStack[] readItem(ItemStack container) {
		NbtCompound entityTag = container.getSubNbt("EntityTag");
		if (entityTag == null)
			entityTag = new NbtCompound();
		return ContainerIO.read(new LocalEntity(getEntityType(container), entityTag));
	}
	
	@Override
	public void writeItem(ItemStack container, ItemStack[] contents) {
		ContainerIO.write(new LocalEntity(getEntityType(container), container.getOrCreateSubNbt("EntityTag")), contents);
	}
	
	private EntityType<?> getEntityType(ItemStack container) {
		return ((SpawnEggItem) container.getItem()).getEntityType(container.getNbt());
	}
	
}
