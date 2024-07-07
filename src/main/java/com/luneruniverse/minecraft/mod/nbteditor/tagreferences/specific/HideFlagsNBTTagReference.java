package com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific;

import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.TagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.HideFlag;

import net.minecraft.item.ItemStack;

public class HideFlagsNBTTagReference implements TagReference<Boolean, ItemStack> {
	
	private final HideFlag flag;
	
	public HideFlagsNBTTagReference(HideFlag flag) {
		this.flag = flag;
	}
	
	@Override
	public Boolean get(ItemStack object) {
		return object.manager$hasNbt() && flag.isEnabled(object.manager$getNbt().getInt("HideFlags"));
	}
	
	@Override
	public void set(ItemStack object, Boolean value) {
		int flags = (object.manager$hasNbt() ? object.manager$getNbt().getInt("HideFlags") : 0);
		flag.set(flags, value);
		if (flags == 0) {
			if (object.manager$hasNbt())
				object.manager$modifyNbt(nbt -> nbt.remove("HideFlags"));
		} else
			object.manager$modifyNbt(nbt -> nbt.putInt("HideFlags", flags));
	}
	
}
