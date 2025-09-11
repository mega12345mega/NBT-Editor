package com.luneruniverse.minecraft.mod.nbteditor.containers;

import java.util.Arrays;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class BundleContentsComponentContainerIO implements ContainerIO<ItemStack> {
	
	private final int maxSlots;
	private final Identifier[] textures;
	
	public BundleContentsComponentContainerIO(int maxSlots) {
		this.maxSlots = maxSlots;
		this.textures = new Identifier[maxSlots];
	}
	
	@Override
	public boolean isSupported(ItemStack container) {
		BundleContentsComponent component = container.get(DataComponentTypes.BUNDLE_CONTENTS);
		return component == null || component.size() <= maxSlots;
	}
	
	@Override
	public int getMaxSlots(ItemStack container) {
		return maxSlots;
	}
	
	@Override
	public Identifier[] getTextures(ItemStack container) {
		return textures;
	}
	
	@Override
	public ItemStack[] read(ItemStack container) {
		BundleContentsComponent component = container.get(DataComponentTypes.BUNDLE_CONTENTS);
		if (component == null)
			return new ItemStack[0];
		return component.stream().toArray(ItemStack[]::new);
	}
	
	@Override
	public int write(ItemStack container, ItemStack[] contents) {
		container.set(DataComponentTypes.BUNDLE_CONTENTS, new BundleContentsComponent(
				Arrays.stream(contents).filter(item -> item != null && !item.isEmpty()).map(ItemStack::copy).toList()));
		return contents.length;
	}
	
	@Override
	public int getNumWritten(ItemStack container, ItemStack[] contents) {
		return contents.length;
	}
	
	@Override
	public int getWrittenSlotIndex(ItemStack container, ItemStack[] contents, int slot) {
		int output = slot;
		for (int i = 0; i < slot; i++) {
			if (contents[i] == null || contents[i].isEmpty())
				output--;
		}
		return output;
	}
	
}
