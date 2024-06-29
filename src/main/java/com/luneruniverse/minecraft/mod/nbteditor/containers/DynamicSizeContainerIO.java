package com.luneruniverse.minecraft.mod.nbteditor.containers;

import java.util.Arrays;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.NBTManagers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

public class DynamicSizeContainerIO implements NBTContainerIO {
	
	private final String key;
	private final int maxNumItems;
	
	public DynamicSizeContainerIO(String key, int maxNumItems) {
		this.key = key;
		this.maxNumItems = maxNumItems;
	}
	
	@Override
	public boolean isNBTReadable(NbtCompound nbt, SourceContainerType source) {
		return nbt.getList(key, NbtElement.COMPOUND_TYPE).size() <= maxNumItems;
	}
	
	@Override
	public ItemStack[] readNBT(NbtCompound container, SourceContainerType source) {
		return container.getList(key, NbtElement.COMPOUND_TYPE).stream().limit(maxNumItems)
				.map(item -> NBTManagers.ITEM.deserialize((NbtCompound) item)).toArray(ItemStack[]::new);
	}
	
	@Override
	public void writeNBT(NbtCompound container, ItemStack[] contents, SourceContainerType source) {
		container.put(key, Arrays.stream(contents).limit(maxNumItems)
				.filter(item -> item != null && !item.isEmpty()).map(ItemStack::manager$serialize)
				.collect(NbtList::new, NbtList::add, NbtList::addAll));
	}
	
}
