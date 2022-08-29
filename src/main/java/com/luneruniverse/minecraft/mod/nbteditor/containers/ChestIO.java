package com.luneruniverse.minecraft.mod.nbteditor.containers;

import com.luneruniverse.minecraft.mod.nbteditor.util.ItemChest;

import net.minecraft.item.ItemStack;

public class ChestIO extends ContainerIO {
	
	ChestIO() {
		
	}
	
	@Override
	public ItemStack[] readItems(ItemStack container) {
		return new ItemChest(container).getAll();
	}
	
	@Override
	public void writeItems(ItemStack container, ItemStack[] contents) {
		new ItemChest(container).setAll(contents);
	}
	
}
