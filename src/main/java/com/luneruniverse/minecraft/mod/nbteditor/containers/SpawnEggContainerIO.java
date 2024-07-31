package com.luneruniverse.minecraft.mod.nbteditor.containers;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalEntity;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.TagNames;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class SpawnEggContainerIO implements ItemContainerIO {
	
	@Override
	public int getMaxItemSize(ItemStack item) {
		if (item == null)
			return 0;
		NbtCompound nbt = item.manager$getNbt();
		NbtCompound entityTag = (nbt == null ? new NbtCompound() : nbt.getCompound(TagNames.ENTITY_TAG));
		return ContainerIO.getMaxSize(new LocalEntity(MVMisc.getEntityType(item), entityTag));
	}
	
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
	public int writeItem(ItemStack container, ItemStack[] contents) {
		LocalEntity entity = new LocalEntity(MVMisc.getEntityType(container),
				container.manager$getOrCreateNbt().getCompound(TagNames.ENTITY_TAG));
		int output = ContainerIO.write(entity, contents);
		container.manager$modifyNbt(nbt -> nbt.put(TagNames.ENTITY_TAG, MainUtil.fillId(entity.getNBT())));
		return output;
	}
	
}
