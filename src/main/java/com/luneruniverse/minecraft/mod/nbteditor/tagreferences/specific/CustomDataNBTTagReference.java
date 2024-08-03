package com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific;

import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.TagReference;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class CustomDataNBTTagReference implements TagReference<NbtCompound, ItemStack> {
	
	@Override
	public NbtCompound get(ItemStack object) {
		NbtCompound nbt = object.manager$getNbt();
		if (nbt == null)
			return new NbtCompound();
		return nbt;
	}
	
	@Override
	public void set(ItemStack object, NbtCompound value) {
		object.manager$setNbt(value);
	}
	
}
