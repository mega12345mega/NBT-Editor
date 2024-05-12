package com.luneruniverse.minecraft.mod.nbteditor.containers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public interface ItemContainerIO {
	public static ItemContainerIO forNBTIO(NBTContainerIO io) {
		return new ItemContainerIO() {
			@Override
			public boolean isItemReadable(ItemStack item) {
				NbtCompound nbt = item.getNbt();
				if (nbt == null)
					nbt = new NbtCompound();
				return io.isNBTReadable(nbt);
			}
			@Override
			public ItemStack[] readItem(ItemStack container) {
				NbtCompound nbt = container.getNbt();
				if (nbt == null)
					nbt = new NbtCompound();
				return io.readNBT(nbt);
			}
			@Override
			public void writeItem(ItemStack container, ItemStack[] contents) {
				io.writeNBT(container.getOrCreateNbt(), contents);
			}
		};
	}
	
	public default boolean isItemReadable(ItemStack item) {
		return true;
	}
	public ItemStack[] readItem(ItemStack container);
	public void writeItem(ItemStack container, ItemStack[] contents);
}
