package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public interface MVItemStackParent {
	public default boolean manager$hasCustomName() {
		throw new RuntimeException("Missing implementation for MVItemStackParent#manager$hasCustomName");
	}
	public default ItemStack manager$setCustomName(Text name) {
		throw new RuntimeException("Missing implementation for MVItemStackParent#manager$setCustomName");
	}
}
