package com.luneruniverse.minecraft.mod.nbteditor.containers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

/**
 * Patches MC-48453
 */
public class ChiseledBookshelfContainerIO extends BlockEntityTagContainerIO {
	
	public ChiseledBookshelfContainerIO() {
		super(new ConstSizeContainerIO(6));
	}
	
	@Override
	public void writeItem(ItemStack container, ItemStack[] contents) {
		super.writeItem(container, contents);
		
		NbtList itemsNbt = container.getSubNbt("BlockEntityTag").getList("Items", NbtElement.COMPOUND_TYPE);
		NbtCompound blockStatesNbt = container.getNbt().getCompound("BlockStateTag");
		boolean[] filledSlots = new boolean[6];
		for (NbtElement itemNbtElement : itemsNbt)
			filledSlots[((NbtCompound) itemNbtElement).getInt("Slot")] = true;
		for (int i = 0; i < 6; i++) {
			String state = "slot_" + i + "_occupied";
			if (filledSlots[i])
				blockStatesNbt.putString(state, "true");
			else
				blockStatesNbt.remove(state);
		}
		container.getNbt().put("BlockStateTag", blockStatesNbt);
	}
	
}
