package com.luneruniverse.minecraft.mod.nbteditor.containers;

import java.lang.reflect.Array;

import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.SlotTexture;

import net.minecraft.item.ItemStack;

public class WithoutSlotRangeContainerIO<T> implements ContainerIO<T> {
	
	private final ContainerIO<T> delegate;
	private final int startSlot;
	private final int endSlot;
	
	public WithoutSlotRangeContainerIO(ContainerIO<T> delegate, int startSlot, int endSlot) {
		this.delegate = delegate;
		this.startSlot = startSlot;
		this.endSlot = endSlot;
	}
	
	private int removeExtra(int slot, boolean length) {
		if (slot < startSlot)
			return slot;
		if (slot < endSlot) {
			if (length)
				return startSlot;
			throw new IllegalArgumentException("Illegal slot: " + slot);
		}
		return slot - (endSlot - startSlot);
	}
	
	private int fillExtra(int slot) {
		if (slot < startSlot)
			return slot;
		return slot + (endSlot - startSlot);
	}
	
	private <C> C[] removeExtra(C[] array) {
		if (array.length <= startSlot)
			return array;
		
		@SuppressWarnings("unchecked")
		C[] output = (C[]) Array.newInstance(array.getClass().componentType(), removeExtra(array.length, true));
		
		System.arraycopy(array, 0, output, 0, startSlot);
		if (array.length > endSlot)
			System.arraycopy(array, endSlot, output, startSlot, array.length - endSlot);
		
		return output;
	}
	
	private <C> C[] fillExtra(C[] array) {
		if (array.length <= startSlot)
			return array;
		
		@SuppressWarnings("unchecked")
		C[] output = (C[]) Array.newInstance(array.getClass().componentType(), array.length + (endSlot - startSlot));
		
		System.arraycopy(array, 0, output, 0, startSlot);
		System.arraycopy(array, startSlot, output, endSlot, array.length - startSlot);
		
		return output;
	}
	
	@Override
	public boolean isSupported(T container) {
		return delegate.isSupported(container);
	}
	
	@Override
	public int getMaxSlots(T container) {
		return removeExtra(delegate.getMaxSlots(container), true);
	}
	
	@Override
	public SlotTexture[] getTextures(T container) {
		return removeExtra(delegate.getTextures(container));
	}
	
	@Override
	public ItemStack[] read(T container) {
		return removeExtra(delegate.read(container));
	}
	
	@Override
	public int write(T container, ItemStack[] contents) {
		return removeExtra(delegate.write(container, fillExtra(contents)), true);
	}
	
	@Override
	public int getNumWritten(T container, ItemStack[] contents) {
		return removeExtra(delegate.getNumWritten(container, fillExtra(contents)), true);
	}
	
	@Override
	public int getWrittenSlotIndex(T container, ItemStack[] contents, int slot) {
		return removeExtra(delegate.getWrittenSlotIndex(container, fillExtra(contents), fillExtra(slot)), false);
	}
	
}
