package com.luneruniverse.minecraft.mod.nbteditor.containers;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalNBT;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

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
		
		if (tag == null || nbtIO.getDefaultEntityId() == null)
			return nbt;
		return nbt.getCompound(tag);
	}
	private void setNBT(ItemStack item, NbtCompound nbt) {
		String defaultEntityId = nbtIO.getDefaultEntityId();
		if (tag == null || defaultEntityId == null)
			item.manager$setNbt(nbt);
		else
			item.manager$modifyNbt(itemNbt -> itemNbt.put(tag, MainUtil.fillId(nbt, defaultEntityId)));
	}
	
	@Override
	public int getMaxItemSize(ItemStack item) {
		return nbtIO.getMaxNBTSize(item == null ? null : getNBT(item), SourceContainerType.ITEM);
	}
	
	@Override
	public boolean isItemReadable(ItemStack item) {
		return nbtIO.isNBTReadable(getNBT(item), SourceContainerType.ITEM);
	}
	
	@Override
	public ItemStack[] readItem(ItemStack container) {
		return nbtIO.readNBT(getNBT(container), SourceContainerType.ITEM);
	}
	
	@Override
	public int writeItem(ItemStack container, ItemStack[] contents) {
		NbtCompound nbt = getNBT(container);
		int output = nbtIO.writeNBT(nbt, contents, SourceContainerType.ITEM);
		setNBT(container, nbt);
		return output;
	}
	
	@Override
	public int getWrittenItemSlotIndex(ItemStack container, ItemStack[] contents, int slot) {
		return nbtIO.getWrittenNBTSlotIndex(getNBT(container), contents, slot, SourceContainerType.ITEM);
	}
	
}
