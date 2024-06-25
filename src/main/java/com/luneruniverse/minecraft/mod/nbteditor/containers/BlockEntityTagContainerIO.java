package com.luneruniverse.minecraft.mod.nbteditor.containers;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalBlock;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class BlockEntityTagContainerIO implements ItemContainerIO, BlockContainerIO {
	
	private final NBTContainerIO nbtIO;
	
	public BlockEntityTagContainerIO(NBTContainerIO nbtIO) {
		this.nbtIO = nbtIO;
	}
	
	@Override
	public boolean isItemReadable(ItemStack item) {
		NbtCompound blockEntityTag = item.getSubNbt("BlockEntityTag");
		if (blockEntityTag == null)
			blockEntityTag = new NbtCompound();
		return nbtIO.isNBTReadable(blockEntityTag, SourceContainerType.ITEM);
	}
	@Override
	public ItemStack[] readItem(ItemStack container) {
		NbtCompound blockEntityTag = container.getSubNbt("BlockEntityTag");
		if (blockEntityTag == null)
			blockEntityTag = new NbtCompound();
		return nbtIO.readNBT(blockEntityTag, SourceContainerType.ITEM);
	}
	@Override
	public void writeItem(ItemStack container, ItemStack[] contents) {
		nbtIO.writeNBT(container.getOrCreateSubNbt("BlockEntityTag"), contents, SourceContainerType.ITEM);
	}
	
	@Override
	public boolean isBlockReadable(LocalBlock block) {
		NbtCompound blockEntityTag = block.getNBT();
		if (blockEntityTag == null)
			blockEntityTag = new NbtCompound();
		return nbtIO.isNBTReadable(blockEntityTag, SourceContainerType.BLOCK);
	}
	@Override
	public ItemStack[] readBlock(LocalBlock container) {
		NbtCompound blockEntityTag = container.getNBT();
		if (blockEntityTag == null)
			blockEntityTag = new NbtCompound();
		return nbtIO.readNBT(blockEntityTag, SourceContainerType.BLOCK);
	}
	@Override
	public void writeBlock(LocalBlock container, ItemStack[] contents) {
		nbtIO.writeNBT(container.getOrCreateNBT(), contents, SourceContainerType.BLOCK);
	}
	
}
