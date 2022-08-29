package com.luneruniverse.minecraft.mod.nbteditor.containers;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

public class EntityIO extends ContainerIO {
	
	EntityIO() {
		
	}
	
	@Override
	public ItemStack[] readItems(ItemStack container) {
		ItemStack[] output = new ItemStack[6];
		
		NbtList armor = container.getOrCreateSubNbt("EntityTag").getList("ArmorItems", NbtType.COMPOUND);
		if (armor.size() == 4) {
			for (int i = 0; i < 4; i++)
				output[i] = ItemStack.fromNbt(armor.getCompound(3 - i));
		}
		
		NbtList hands = container.getSubNbt("EntityTag").getList("HandItems", NbtType.COMPOUND);
		if (hands.size() == 2) {
			for (int i = 0; i < 2; i++)
				output[i + 4] = ItemStack.fromNbt(hands.getCompound(i));
		}
		
		return output;
	}
	
	@Override
	public void writeItems(ItemStack container, ItemStack[] contents) {
		NbtList armor = new NbtList();
		for (int i = 0; i < 4; i++)
			armor.add(contents[3 - i].writeNbt(new NbtCompound()));
		container.getOrCreateSubNbt("EntityTag").put("ArmorItems", armor);
		
		NbtList hands = new NbtList();
		for (int i = 0; i < 2; i++)
			hands.add(contents[i + 4].writeNbt(new NbtCompound()));
		container.getSubNbt("EntityTag").put("HandItems", hands);
	}
	
}
