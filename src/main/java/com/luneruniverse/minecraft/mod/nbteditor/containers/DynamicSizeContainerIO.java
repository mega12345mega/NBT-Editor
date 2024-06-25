package com.luneruniverse.minecraft.mod.nbteditor.containers;

import java.util.Arrays;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

public class DynamicSizeContainerIO implements NBTContainerIO {
	
	private final int maxNumItems;
	
	public DynamicSizeContainerIO(int maxNumItems) {
		this.maxNumItems = maxNumItems;
	}
	
	@Override
	public boolean isNBTReadable(NbtCompound nbt, SourceContainerType source) {
		return nbt.getList("Items", NbtElement.COMPOUND_TYPE).size() <= maxNumItems;
	}
	
	@Override
	public ItemStack[] readNBT(NbtCompound container, SourceContainerType source) {
		return container.getList("Items", NbtElement.COMPOUND_TYPE).stream().limit(maxNumItems)
				.map(item -> ItemStack.fromNbt((NbtCompound) item)).toArray(ItemStack[]::new);
	}
	
	@Override
	public void writeNBT(NbtCompound container, ItemStack[] contents, SourceContainerType source) {
		container.put("Items", Arrays.stream(contents).limit(maxNumItems)
				.filter(item -> item != null && !item.isEmpty()).map(item -> item.writeNbt(new NbtCompound()))
				.collect(NbtList::new, NbtList::add, NbtList::addAll));
	}
	
}
