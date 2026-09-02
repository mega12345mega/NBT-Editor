package com.luneruniverse.minecraft.mod.nbteditor.containers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class OldHorseArmorHandsContainerIO extends WithoutSlotRangeContainerIO<NbtCompound> {
	
	public OldHorseArmorHandsContainerIO(int armorSlot) {
		super(new DelegateContainerIO<>(new ArmorHandsContainerIO()) {
			@Override
			public int write(NbtCompound container, ItemStack[] contents) {
				if (armorSlot < contents.length)
					contents[1] = contents[armorSlot];
				return super.write(container, contents);
			}
		}, 1, 2);
	}
	
}
