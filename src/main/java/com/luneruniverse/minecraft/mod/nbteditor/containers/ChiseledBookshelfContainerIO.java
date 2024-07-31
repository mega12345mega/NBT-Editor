package com.luneruniverse.minecraft.mod.nbteditor.containers;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalBlock;
import com.luneruniverse.minecraft.mod.nbteditor.misc.BlockStateProperties;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.TagNames;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

/**
 * Patches MC-48453
 */
public class ChiseledBookshelfContainerIO extends BlockEntityTagContainerIO {
	
	public ChiseledBookshelfContainerIO() {
		super(new ConstSizeContainerIO(6));
	}
	
	@Override
	public int writeItem(ItemStack container, ItemStack[] contents) {
		int output = super.writeItem(container, contents);
		
		contents = readItem(container);
		NbtCompound blockStatesTag = container.manager$getNbt().getCompound(TagNames.BLOCK_STATE_TAG);
		for (int i = 0; i < 6; i++) {
			String state = "slot_" + i + "_occupied";
			if (contents[i] != null && !contents[i].isEmpty())
				blockStatesTag.putString(state, "true");
			else
				blockStatesTag.remove(state);
		}
		container.manager$modifyNbt(nbt -> nbt.put(TagNames.BLOCK_STATE_TAG, blockStatesTag));
		
		return output;
	}
	
	@Override
	public int writeBlock(LocalBlock container, ItemStack[] contents) {
		int output = super.writeBlock(container, contents);
		
		contents = readBlock(container);
		BlockStateProperties state = container.getState();
		for (int i = 0; i < 6; i++)
			state.setValue("slot_" + i + "_occupied", contents[i] != null && !contents[i].isEmpty() ? "true" : "false");
		
		return output;
	}
	
}
