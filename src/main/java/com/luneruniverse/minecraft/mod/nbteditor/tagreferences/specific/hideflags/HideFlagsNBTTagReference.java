package com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.hideflags;

import java.util.LinkedHashMap;
import java.util.Map;

import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.TagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.hideflags.HideFlag;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.hideflags.NBTHideFlag;

import net.minecraft.item.ItemStack;

public class HideFlagsNBTTagReference implements TagReference<Map<HideFlag, Boolean>, ItemStack> {
	
	@Override
	public Map<HideFlag, Boolean> get(ItemStack object) {
		int flags = (object.nbte$hasNbt() ? object.nbte$getNbt().nbte$getIntOrDefault("HideFlags") : 0);
		
		Map<HideFlag, Boolean> output = new LinkedHashMap<>();
		for (Map.Entry<Integer, HideFlag> flag : NBTHideFlag.FLAGS.entrySet())
			output.put(flag.getValue(), (flags & flag.getKey()) != 0);
		return output;
	}
	
	@Override
	public void set(ItemStack object, Map<HideFlag, Boolean> value) {
		if (value.isEmpty())
			return;
		
		int flags = (object.nbte$hasNbt() ? object.nbte$getNbt().nbte$getIntOrDefault("HideFlags") : 0);
		
		for (Map.Entry<HideFlag, Boolean> flag : value.entrySet()) {
			int bit = ((NBTHideFlag) flag.getKey()).getBit();
			flags = flag.getValue() ? (flags | bit) : (flags & ~bit);
		}
		
		if (flags == 0) {
			if (object.nbte$hasNbt())
				object.nbte$modifyNbt(nbt -> nbt.remove("HideFlags"));
		} else {
			final int finalFlags = flags;
			object.nbte$modifyNbt(nbt -> nbt.putInt("HideFlags", finalFlags));
		}
	}
	
}
