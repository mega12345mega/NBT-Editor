package com.luneruniverse.minecraft.mod.nbteditor.containers;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class ConcatNBTContainerIO implements NBTContainerIO {
	
	private final NBTContainerIO[] nbtIOs;
	
	public ConcatNBTContainerIO(NBTContainerIO... nbtIOs) {
		this.nbtIOs = nbtIOs;
	}
	
	@Override
	public boolean passRootNbt(SourceContainerType source) {
		if (nbtIOs.length == 0)
			return NBTContainerIO.super.passRootNbt(source);
		boolean output = nbtIOs[0].passRootNbt(source);
		for (int i = 1; i < nbtIOs.length; i++) {
			if (nbtIOs[i].passRootNbt(source) != output)
				throw new IllegalStateException("Concated containers disagree on passRootNbt!");
		}
		return output;
	}
	
	@Override
	public int getMaxNBTSize(NbtCompound nbt, SourceContainerType source) {
		int total = 0;
		for (NBTContainerIO nbtIO : nbtIOs) {
			int size = nbtIO.getMaxNBTSize(nbt, source);
			if (size == 0)
				return 0;
			total += size;
		}
		return total;
	}
	
	@Override
	public boolean isNBTReadable(NbtCompound nbt, SourceContainerType source) {
		for (NBTContainerIO nbtIO : nbtIOs) {
			if (!nbtIO.isNBTReadable(nbt, source))
				return false;
		}
		return true;
	}
	
	@Override
	public ItemStack[] readNBT(NbtCompound container, SourceContainerType source) {
		List<ItemStack> output = new ArrayList<>();
		for (NBTContainerIO nbtIO : nbtIOs) {
			for (ItemStack item : nbtIO.readNBT(container, source))
				output.add(item);
		}
		return output.toArray(ItemStack[]::new);
	}
	
	@Override
	public int writeNBT(NbtCompound container, ItemStack[] contents, SourceContainerType source) {
		int total = 0;
		for (NBTContainerIO nbtIO : nbtIOs) {
			int numWritten = nbtIO.writeNBT(container, contents, source);
			if (numWritten >= contents.length)
				contents = new ItemStack[0];
			else {
				ItemStack[] temp = new ItemStack[contents.length - numWritten];
				System.arraycopy(contents, numWritten, temp, 0, temp.length);
				contents = temp;
			}
			total += numWritten;
		}
		return total;
	}
	
}
