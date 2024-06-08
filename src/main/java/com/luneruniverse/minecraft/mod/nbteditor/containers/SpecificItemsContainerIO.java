package com.luneruniverse.minecraft.mod.nbteditor.containers;

import java.util.stream.Stream;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class SpecificItemsContainerIO implements NBTContainerIO {
	
	private final String[] keys;
	
	public SpecificItemsContainerIO(String... keys) {
		this.keys = keys;
	}
	
	private ItemStack readKey(NbtCompound container, String key) {
		if (!container.contains(key, NbtElement.COMPOUND_TYPE))
			return null;
		return ItemStack.fromNbt(container.getCompound(key));
	}
	private void writeKey(NbtCompound container, String key, ItemStack item, SourceContainerType source) {
		if (item == null || item.isEmpty()) {
			if (source == SourceContainerType.ITEM)
				container.remove(key);
			else
				container.put(key, ItemStack.EMPTY.writeNbt(new NbtCompound()));
		} else
			container.put(key, item.writeNbt(new NbtCompound()));
	}
	
	@Override
	public ItemStack[] readNBT(NbtCompound container, SourceContainerType source) {
		return Stream.of(keys).map(key -> readKey(container, key)).toArray(ItemStack[]::new);
	}
	
	@Override
	public void writeNBT(NbtCompound container, ItemStack[] contents, SourceContainerType source) {
		for (int i = 0; i < keys.length; i++) {
			ItemStack item = null;
			if (i < contents.length)
				item = contents[i];
			writeKey(container, keys[i], item, source);
		}
	}
	
}
