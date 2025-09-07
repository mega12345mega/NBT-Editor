package com.luneruniverse.minecraft.mod.nbteditor.containers;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.manager.NBTManagers;

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
	public String getDefaultEntityId() {
		return NBTManagers.COMPONENTS_EXIST ? null : "";
	}
	
	@Override
	public int getMaxNBTSize(NbtCompound nbt, SourceContainerType source) {
		return numItems;
	}
	
	@Override
	public ItemStack[] readNBT(NbtCompound container, SourceContainerType source) {
		boolean itemComponent = NBTManagers.COMPONENTS_EXIST && source == SourceContainerType.ITEM;
		
		ItemStack[] items = new ItemStack[numItems];
		NbtList itemsNbt = container.nbte$getListOrDefault(itemComponent ? "minecraft:container" : "Items", NbtElement.COMPOUND_TYPE);
		for (NbtElement itemNbtElement : itemsNbt.nbte$iterable()) {
			NbtCompound itemNbt = (NbtCompound) itemNbtElement;
			int slot = itemNbt.nbte$getIntOrDefault(itemComponent ? "slot" : "Slot");
			if (slot < 0 || slot >= numItems)
				continue;
			items[slot] = NBTManagers.ITEM.deserialize(itemComponent ? itemNbt.nbte$getCompoundOrDefault("item") : itemNbt, true);
		}
		return items;
	}
	
	@Override
	public int writeNBT(NbtCompound container, ItemStack[] contents, SourceContainerType source) {
		boolean itemComponent = NBTManagers.COMPONENTS_EXIST && source == SourceContainerType.ITEM;
		
		NbtList itemsNbt = new NbtList();
		for (int i = 0; i < contents.length; i++) {
			ItemStack item = contents[i];
			if (item == null || item.isEmpty())
				continue;
			NbtCompound itemNbt = item.nbte$serialize(true);
			if (itemComponent) {
				NbtCompound wrapper = new NbtCompound();
				wrapper.put("item", itemNbt);
				itemNbt = wrapper;
			}
			String slotKey = (itemComponent ? "slot" : "Slot");
			if (i < 128)
				itemNbt.putByte(slotKey, (byte) i);
			else
				itemNbt.putInt(slotKey, i);
			itemsNbt.add(itemNbt);
		}
		container.put(itemComponent ? "minecraft:container" : "Items", itemsNbt);
		
		return numItems;
	}
	
}
