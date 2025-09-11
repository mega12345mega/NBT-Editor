package com.luneruniverse.minecraft.mod.nbteditor.containers;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.manager.NBTManagers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

public class KeysContainerIO implements ContainerIO<NbtCompound> {
	
	private final boolean removeWhenEmpty;
	private final String[] keys;
	private final Identifier[] textures;
	
	public KeysContainerIO(boolean removeWhenEmpty, String... keys) {
		this.removeWhenEmpty = removeWhenEmpty;
		this.keys = keys;
		this.textures = new Identifier[keys.length];
	}
	
	@Override
	public boolean isSupported(NbtCompound container) {
		for (String key : keys) {
			NbtElement itemNbtElement = container.get(key);
			if (itemNbtElement != null && !(itemNbtElement instanceof NbtCompound))
				return false;
		}
		return true;
	}
	
	@Override
	public int getMaxSlots(NbtCompound container) {
		return keys.length;
	}
	
	@Override
	public Identifier[] getTextures(NbtCompound container) {
		return textures;
	}
	
	@Override
	public ItemStack[] read(NbtCompound container) {
		ItemStack[] contents = new ItemStack[keys.length];
		for (int i = 0; i < keys.length; i++) {
			contents[i] = container.nbte$getCompound(keys[i])
					.map(itemNbt -> NBTManagers.ITEM.deserializeOrElse(itemNbt, ItemStack.EMPTY)).orElse(ItemStack.EMPTY);
		}
		return contents;
	}
	
	@Override
	public int write(NbtCompound container, ItemStack[] contents) {
		for (int i = 0; i < keys.length; i++) {
			ItemStack item = contents[i];
			if (item == null || item.isEmpty()) {
				if (removeWhenEmpty) {
					container.remove(keys[i]);
					continue;
				} else {
					item = ItemStack.EMPTY;
				}
			}
			container.put(keys[i], item.nbte$serialize(true));
		}
		return keys.length;
	}
	
	@Override
	public int getNumWritten(NbtCompound container, ItemStack[] contents) {
		return keys.length;
	}
	
	@Override
	public int getWrittenSlotIndex(NbtCompound container, ItemStack[] contents, int slot) {
		return slot;
	}
	
}
