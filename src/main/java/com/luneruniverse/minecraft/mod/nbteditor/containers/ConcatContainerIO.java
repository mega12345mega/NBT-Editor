package com.luneruniverse.minecraft.mod.nbteditor.containers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class ConcatContainerIO<T> implements ContainerIO<T> {
	
	private final ContainerIO<T>[] ios;
	
	@SafeVarargs
	public ConcatContainerIO(ContainerIO<T>... ios) {
		this.ios = ios;
	}
	
	@Override
	public boolean isSupported(T container) {
		for (ContainerIO<T> io : ios) {
			if (!io.isSupported(container))
				return false;
		}
		return true;
	}
	
	@Override
	public int getMaxSlots(T container) {
		int maxSlots = 0;
		for (ContainerIO<T> io : ios)
			maxSlots += io.getMaxSlots(container);
		return maxSlots;
	}
	
	@Override
	public Identifier[] getTextures(T container) {
		List<Identifier> textures = new ArrayList<>();
		for (ContainerIO<T> io : ios)
			textures.addAll(Arrays.asList(io.getTextures(container)));
		return textures.toArray(Identifier[]::new);
	}
	
	@Override
	public ItemStack[] read(T container) {
		List<ItemStack> contents = new ArrayList<>();
		for (ContainerIO<T> io : ios)
			contents.addAll(Arrays.asList(io.read(container)));
		return contents.toArray(ItemStack[]::new);
	}
	
	@Override
	public int write(T container, ItemStack[] contents) {
		int numWritten = 0;
		for (ContainerIO<T> io : ios) {
			int currentWritten = io.write(container, contents);
			if (currentWritten >= contents.length) {
				contents = new ItemStack[0];
			} else {
				ItemStack[] temp = new ItemStack[contents.length - currentWritten];
				System.arraycopy(contents, currentWritten, temp, 0, temp.length);
				contents = temp;
			}
			numWritten += currentWritten;
		}
		return numWritten;
	}
	
	@Override
	public int getNumWritten(T container, ItemStack[] contents) {
		int numWritten = 0;
		for (ContainerIO<T> io : ios) {
			int currentWritten = io.getNumWritten(container, contents);
			if (currentWritten >= contents.length) {
				contents = new ItemStack[0];
			} else {
				ItemStack[] temp = new ItemStack[contents.length - currentWritten];
				System.arraycopy(contents, currentWritten, temp, 0, temp.length);
				contents = temp;
			}
			numWritten += currentWritten;
		}
		return numWritten;
	}
	
	@Override
	public int getWrittenSlotIndex(T container, ItemStack[] contents, int slot) {
		int numWritten = 0;
		for (ContainerIO<T> io : ios) {
			int currentWritten = io.getNumWritten(container, contents);
			if (slot < numWritten + currentWritten)
				return io.getWrittenSlotIndex(container, contents, slot - numWritten) + numWritten;
			if (currentWritten >= contents.length) {
				contents = new ItemStack[0];
			} else {
				ItemStack[] temp = new ItemStack[contents.length - currentWritten];
				System.arraycopy(contents, currentWritten, temp, 0, temp.length);
				contents = temp;
			}
			numWritten += currentWritten;
		}
		throw new IllegalArgumentException("Slot is never written: " + slot);
	}
	
}
