package com.luneruniverse.minecraft.mod.nbteditor.containers;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalBlock;
import com.luneruniverse.minecraft.mod.nbteditor.misc.BlockStateProperties;

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
		
		boolean[] filledSlots = getFilledSlots(container.getSubNbt("BlockEntityTag"));
		NbtCompound blockStatesTag = container.getNbt().getCompound("BlockStateTag");
		for (int i = 0; i < 6; i++) {
			String state = "slot_" + i + "_occupied";
			if (filledSlots[i])
				blockStatesTag.putString(state, "true");
			else
				blockStatesTag.remove(state);
		}
		container.getNbt().put("BlockStateTag", blockStatesTag);
	}
	
	@Override
	public void writeBlock(LocalBlock container, ItemStack[] contents) {
		super.writeBlock(container, contents);
		
		boolean[] filledSlots = getFilledSlots(container.getOrCreateNBT());
		BlockStateProperties state = container.getState();
		for (int i = 0; i < 6; i++)
			state.setValue("slot_" + i + "_occupied", filledSlots[i] ? "true" : "false");
	}
	
	private boolean[] getFilledSlots(NbtCompound blockEntityTag) {
		NbtList itemsNbt = blockEntityTag.getList("Items", NbtElement.COMPOUND_TYPE);
		boolean[] filledSlots = new boolean[6];
		for (NbtElement itemNbtElement : itemsNbt)
			filledSlots[((NbtCompound) itemNbtElement).getInt("Slot")] = true;
		return filledSlots;
	}
	
}
