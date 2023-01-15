package com.luneruniverse.minecraft.mod.nbteditor.containers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

public class ItemsContainerIO extends ContainerIO {
	
	private final boolean entity;
	private final int numItems;
	
	ItemsContainerIO(boolean entity, int numItems) {
		this.entity = entity;
		this.numItems = numItems;
	}
	
	@Override
	public ItemStack[] readItems(ItemStack container) {
		ItemStack[] output = new ItemStack[numItems];
		NbtList items = container.getOrCreateSubNbt(entity ? "EntityTag" : "BlockEntityTag").getList("Items", NbtElement.COMPOUND_TYPE);
		for (NbtElement item : items)
			output[((NbtCompound) item).getInt("Slot")] = ItemStack.fromNbt((NbtCompound) item);
		return output;
	}
	
	@Override
	public void writeItems(ItemStack container, ItemStack[] contents) {
		NbtList items = new NbtList();
		for (int i = 0; i < numItems; i++) {
			ItemStack item = (contents[i] == null ? ItemStack.EMPTY : contents[i]);
			if (!item.isEmpty()) {
				NbtCompound nbt = new NbtCompound();
				nbt.putInt("Slot", i);
				items.add(item.writeNbt(nbt));
			}
		}
		container.getOrCreateSubNbt(entity ? "EntityTag" : "BlockEntityTag").put("Items", items);
	}
	
}
