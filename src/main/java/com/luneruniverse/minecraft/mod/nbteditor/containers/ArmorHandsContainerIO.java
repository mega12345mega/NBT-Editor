package com.luneruniverse.minecraft.mod.nbteditor.containers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

public class ArmorHandsContainerIO implements NBTContainerIO {
	
	@Override
	public ItemStack[] readNBT(NbtCompound container, SourceContainerType source) {
		ItemStack[] items = new ItemStack[6];
		
		NbtList armorItemsNbt = container.getList("ArmorItems", NbtElement.COMPOUND_TYPE);
		for (int i = 0; i < armorItemsNbt.size() && i < 4; i++)
			items[3 - i] = ItemStack.fromNbt(armorItemsNbt.getCompound(i));
		
		NbtList handItemsNbt = container.getList("HandItems", NbtElement.COMPOUND_TYPE);
		for (int i = 0; i < handItemsNbt.size() && i < 2; i++)
			items[4 + i] = ItemStack.fromNbt(handItemsNbt.getCompound(i));
		
		return items;
	}
	
	@Override
	public void writeNBT(NbtCompound container, ItemStack[] contents, SourceContainerType source) {
		NbtList armorItemsNbt = new NbtList();
		for (int i = 0; i < 4; i++)
			armorItemsNbt.add(contents[3 - i].writeNbt(new NbtCompound()));
		container.put("ArmorItems", armorItemsNbt);
		
		NbtList handItemsNbt = new NbtList();
		for (int i = 0; i < 2; i++)
			handItemsNbt.add(contents[4 + i].writeNbt(new NbtCompound()));
		container.put("HandItems", handItemsNbt);
	}
	
}
