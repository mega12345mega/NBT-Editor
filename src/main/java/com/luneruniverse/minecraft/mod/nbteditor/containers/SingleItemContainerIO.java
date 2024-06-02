package com.luneruniverse.minecraft.mod.nbteditor.containers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class SingleItemContainerIO implements NBTContainerIO {
	
	private final String key;
	
	public SingleItemContainerIO(String key) {
		this.key = key;
	}
	
	@Override
	public ItemStack[] readNBT(NbtCompound container, SourceContainerType source) {
		if (!container.contains(key, NbtElement.COMPOUND_TYPE))
			return new ItemStack[] {null};
		return new ItemStack[] {ItemStack.fromNbt(container.getCompound(key))};
	}
	
	@Override
	public void writeNBT(NbtCompound container, ItemStack[] contents, SourceContainerType source) {
		if (contents.length == 0 || contents[0] == null || contents[0].isEmpty()) {
			if (source == SourceContainerType.ITEM)
				container.remove(key);
			else
				container.put(key, ItemStack.EMPTY.writeNbt(new NbtCompound()));
		} else
			container.put(key, contents[0].writeNbt(new NbtCompound()));
	}
	
}
