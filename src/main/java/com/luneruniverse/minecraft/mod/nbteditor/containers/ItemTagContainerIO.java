package com.luneruniverse.minecraft.mod.nbteditor.containers;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalNBT;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class ItemTagContainerIO implements ItemContainerIO {
	
	protected static NbtCompound getNBT(LocalNBT localNBT) {
		NbtCompound nbt = localNBT.getNBT();
		return (nbt == null ? new NbtCompound() : nbt);
	}
	
	private final String tag;
	private final NBTContainerIO nbtIO;
	
	public ItemTagContainerIO(String tag, NBTContainerIO nbtIO) {
		this.tag = tag;
		this.nbtIO = nbtIO;
	}
	
	private NbtCompound getNBT(ItemStack item) {
		NbtCompound nbt = item.manager$getNbt();
		if (nbt == null)
			nbt = new NbtCompound();
		
		if (tag == null || nbtIO.passRootNbt(SourceContainerType.ITEM))
			return nbt;
		return nbt.getCompound(tag);
	}
	private void setNBT(ItemStack item, NbtCompound nbt) {
		if (tag == null || nbtIO.passRootNbt(SourceContainerType.ITEM))
			item.manager$setNbt(nbt);
		else
			item.manager$modifyNbt(itemNbt -> itemNbt.put(tag, nbt));
	}
	
	@Override
	public int getMaxItemSize(ItemStack item) {
		return nbtIO.getMaxNBTSize(item == null ? null : getNBT(item), SourceContainerType.ITEM);
	}
	
	@Override
	public ItemStack[] readItem(ItemStack container) {
		return nbtIO.readNBT(getNBT(container), SourceContainerType.ITEM);
	}
	
	@Override
	public void writeItem(ItemStack container, ItemStack[] contents) {
		NbtCompound nbt = getNBT(container);
		nbtIO.writeNBT(nbt, contents, SourceContainerType.ITEM);
		setNBT(container, nbt);
	}
	
}
