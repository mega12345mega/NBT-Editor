package com.luneruniverse.minecraft.mod.nbteditor.containers;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalEntity;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.TagNames;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class SpawnEggContainerIO implements ItemContainerIO {
	
	@Override
	public boolean isItemReadable(ItemStack item) {
		NbtCompound nbt = item.manager$getNbt();
		NbtCompound entityTag = (nbt == null ? new NbtCompound() : nbt.getCompound(TagNames.ENTITY_TAG));
		return ContainerIO.isContainer(new LocalEntity(MVMisc.getEntityType(item), entityTag));
	}
	
	@Override
	public ItemStack[] readItem(ItemStack container) {
		NbtCompound nbt = container.manager$getNbt();
		NbtCompound entityTag = (nbt == null ? new NbtCompound() : nbt.getCompound(TagNames.ENTITY_TAG));
		return ContainerIO.read(new LocalEntity(MVMisc.getEntityType(container), entityTag));
	}
	
	@Override
	public void writeItem(ItemStack container, ItemStack[] contents) {
		LocalEntity entity = new LocalEntity(MVMisc.getEntityType(container), new NbtCompound());
		ContainerIO.write(entity, contents);
		container.manager$modifyNbt(nbt -> nbt.put(TagNames.ENTITY_TAG, entity.getNBT()));
	}
	
}
