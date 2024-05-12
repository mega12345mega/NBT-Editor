package com.luneruniverse.minecraft.mod.nbteditor.containers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

public class ConstSizeContainerIO implements NBTContainerIO {
	
	private final int numItems;
	
	public ConstSizeContainerIO(int numItems) {
		this.numItems = numItems;
	}
	
	@Override
	public ItemStack[] readNBT(NbtCompound container) {
		ItemStack[] items = new ItemStack[numItems];
		NbtList itemsNbt = container.getList("Items", NbtElement.COMPOUND_TYPE);
		for (NbtElement itemNbtElement : itemsNbt) {
			NbtCompound itemNbt = (NbtCompound) itemNbtElement;
			int slot = itemNbt.getInt("Slot");
			if (slot < 0 || slot >= numItems)
				continue;
			items[slot] = ItemStack.fromNbt(itemNbt);
		}
		return items;
	}
	
	@Override
	public void writeNBT(NbtCompound container, ItemStack[] contents) {
		NbtList itemsNbt = new NbtList();
		for (int i = 0; i < contents.length; i++) {
			ItemStack item = contents[i];
			if (item == null || item.isEmpty())
				continue;
			NbtCompound itemNbt = new NbtCompound();
			item.writeNbt(itemNbt);
			if (i < 128)
				itemNbt.putByte("Slot", (byte) i);
			else
				itemNbt.putInt("Slot", i);
			itemsNbt.add(itemNbt);
		}
		container.put("Items", itemsNbt);
	}
	
}
