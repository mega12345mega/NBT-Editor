package com.luneruniverse.minecraft.mod.nbteditor.containers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class SingleItemContainerIO extends ContainerIO {
	
	private final boolean entity;
	private final String key;
	
	public SingleItemContainerIO(boolean entity, String key) {
		this.entity = entity;
		this.key = key;
	}
	
	@Override
	public ItemStack[] readItems(ItemStack container) {
		NbtCompound item = container.getOrCreateSubNbt(entity ? "EntityTag" : "BlockEntityTag").getCompound(key);
		if (item.isEmpty())
			return new ItemStack[] {ItemStack.EMPTY};
		return new ItemStack[] {ItemStack.fromNbt(item)};
	}
	
	@Override
	public void writeItems(ItemStack container, ItemStack[] contents) {
		NbtCompound tag = container.getOrCreateSubNbt(entity ? "EntityTag" : "BlockEntityTag");
		if (contents[0] == null || contents[0].isEmpty())
			tag.remove(key);
		else
			tag.put(key, contents[0].writeNbt(new NbtCompound()));
	}
	
}
