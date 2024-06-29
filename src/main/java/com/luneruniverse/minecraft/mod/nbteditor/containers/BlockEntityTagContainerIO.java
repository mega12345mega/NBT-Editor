package com.luneruniverse.minecraft.mod.nbteditor.containers;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalBlock;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.TagNames;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class BlockEntityTagContainerIO implements ItemContainerIO, BlockContainerIO {
	
	private final NBTContainerIO itemNbtIO;
	private final NBTContainerIO blockNbtIO;
	
	public BlockEntityTagContainerIO(NBTContainerIO itemNbtIO, NBTContainerIO blockNbtIO) {
		this.itemNbtIO = itemNbtIO;
		this.blockNbtIO = blockNbtIO;
	}
	public BlockEntityTagContainerIO(NBTContainerIO nbtIO) {
		this(nbtIO, nbtIO);
	}
	
	@Override
	public boolean isItemReadable(ItemStack item) {
		NbtCompound nbt = item.manager$getNbt();
		NbtCompound blockEntityTag = (nbt == null ? new NbtCompound() : nbt.getCompound(TagNames.BLOCK_ENTITY_TAG));
		return itemNbtIO.isNBTReadable(blockEntityTag, SourceContainerType.ITEM);
	}
	@Override
	public ItemStack[] readItem(ItemStack container) {
		NbtCompound nbt = container.manager$getNbt();
		NbtCompound blockEntityTag = (nbt == null ? new NbtCompound() : nbt.getCompound(TagNames.BLOCK_ENTITY_TAG));
		return itemNbtIO.readNBT(blockEntityTag, SourceContainerType.ITEM);
	}
	@Override
	public void writeItem(ItemStack container, ItemStack[] contents) {
		container.manager$modifySubNbt(TagNames.BLOCK_ENTITY_TAG,
				blockEntityTag -> itemNbtIO.writeNBT(blockEntityTag, contents, SourceContainerType.ITEM));
	}
	
	@Override
	public boolean isBlockReadable(LocalBlock block) {
		NbtCompound blockEntityTag = block.getNBT();
		if (blockEntityTag == null)
			blockEntityTag = new NbtCompound();
		return blockNbtIO.isNBTReadable(blockEntityTag, SourceContainerType.BLOCK);
	}
	@Override
	public ItemStack[] readBlock(LocalBlock container) {
		NbtCompound blockEntityTag = container.getNBT();
		if (blockEntityTag == null)
			blockEntityTag = new NbtCompound();
		return blockNbtIO.readNBT(blockEntityTag, SourceContainerType.BLOCK);
	}
	@Override
	public void writeBlock(LocalBlock container, ItemStack[] contents) {
		blockNbtIO.writeNBT(container.getOrCreateNBT(), contents, SourceContainerType.BLOCK);
	}
	
}
