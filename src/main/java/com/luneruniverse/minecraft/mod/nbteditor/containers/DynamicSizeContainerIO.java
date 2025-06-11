package com.luneruniverse.minecraft.mod.nbteditor.containers;

import java.util.Arrays;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.NBTManagers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

public class DynamicSizeContainerIO implements NonItemNBTContainerIO {
	
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
				.map(item -> NBTManagers.ITEM.deserialize((NbtCompound) item, true)).toArray(ItemStack[]::new);
	}
	
	@Override
	public int writeNBT(NbtCompound container, ItemStack[] contents, SourceContainerType source) {
		NbtList nbt = Arrays.stream(contents).limit(maxSize)
				.filter(item -> item != null && !item.isEmpty()).map(item -> item.manager$serialize(true))
				.collect(NbtList::new, NbtList::add, NbtList::addAll);
		container.put(key, nbt);
		return Math.min(contents.length, maxSize);
	}
	
	@Override
	public int getWrittenNBTSlotIndex(NbtCompound container, ItemStack[] contents, int slot, SourceContainerType source) {
		int output = slot;
		for (int i = 0; i < slot; i++) {
			if (contents[i] == null || contents[i].isEmpty())
				output--;
		}
		return output;
	}
	
}
