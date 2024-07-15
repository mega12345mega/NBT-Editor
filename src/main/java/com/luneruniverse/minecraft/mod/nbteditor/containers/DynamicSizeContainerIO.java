package com.luneruniverse.minecraft.mod.nbteditor.containers;

import java.util.Arrays;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.NBTManagers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

public class DynamicSizeContainerIO implements NBTContainerIO {
	
	private final String key;
	private final int maxSize;
	
	public DynamicSizeContainerIO(String key, int maxSize) {
		this.key = key;
		this.maxSize = maxSize;
	}
	
	@Override
	public int getMaxNBTSize(NbtCompound nbt, SourceContainerType source) {
		return maxSize;
	}
	
	@Override
	public boolean isNBTReadable(NbtCompound nbt, SourceContainerType source) {
		return nbt.getList(key, NbtElement.COMPOUND_TYPE).size() <= maxSize;
	}
	
	@Override
	public ItemStack[] readNBT(NbtCompound container, SourceContainerType source) {
		return container.getList(key, NbtElement.COMPOUND_TYPE).stream().limit(maxSize)
				.map(item -> NBTManagers.ITEM.deserialize((NbtCompound) item)).toArray(ItemStack[]::new);
	}
	
	@Override
	public void writeNBT(NbtCompound container, ItemStack[] contents, SourceContainerType source) {
		container.put(key, Arrays.stream(contents).limit(maxSize)
				.filter(item -> item != null && !item.isEmpty()).map(ItemStack::manager$serialize)
				.collect(NbtList::new, NbtList::add, NbtList::addAll));
	}
	
}
