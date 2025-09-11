package com.luneruniverse.minecraft.mod.nbteditor.containers;

import java.util.Arrays;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class ContainerComponentContainerIO implements ContainerIO<ItemStack> {
	
	private final int numSlots;
	private final Identifier[] textures;
	
	public ContainerComponentContainerIO(int numSlots) {
		this.numSlots = numSlots;
		this.textures = new Identifier[numSlots];
	}
	
	@Override
	public boolean isSupported(ItemStack container) {
		ContainerComponent component = container.get(DataComponentTypes.CONTAINER);
		return component == null || component.stream().count() <= numSlots;
	}
	
	@Override
	public int getMaxSlots(ItemStack container) {
		return numSlots;
	}
	
	@Override
	public Identifier[] getTextures(ItemStack container) {
		return textures;
	}
	
	@Override
	public ItemStack[] read(ItemStack container) {
		return container.get(DataComponentTypes.CONTAINER).stream().toArray(ItemStack[]::new);
	}
	
	@Override
	public int write(ItemStack container, ItemStack[] contents) {
		container.set(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(Arrays.asList(contents)));
		return numSlots;
	}
	
	@Override
	public int getNumWritten(ItemStack container, ItemStack[] contents) {
		return numSlots;
	}
	
	@Override
	public int getWrittenSlotIndex(ItemStack container, ItemStack[] contents, int slot) {
		return slot;
	}
	
}
