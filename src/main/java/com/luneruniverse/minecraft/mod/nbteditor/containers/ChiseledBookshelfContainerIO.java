package com.luneruniverse.minecraft.mod.nbteditor.containers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

/**
 * Patches MC-48453
 */
public class ChiseledBookshelfContainerIO extends ConstSizeContainerIO {
	
	public ChiseledBookshelfContainerIO() {
		super(Target.BLOCK_ENTITY, 6);
	}
	
	@Override
	public void writeItems(ItemStack container, ItemStack[] contents) {
		super.writeItems(container, contents);
		
		NbtList items = target.getItemsParent(container).getList("Items", NbtElement.COMPOUND_TYPE);
		NbtCompound blockStates = container.getNbt().getCompound("BlockStateTag");
		for (int i = 0; i < 6; i++) {
			final int finalI = i;
			boolean filled = items.stream().anyMatch(item -> ((NbtCompound) item).getInt("Slot") == finalI);
			String state = "slot_" + i + "_occupied";
			
			if (filled)
				blockStates.putString(state, "true");
			else
				blockStates.remove(state);
		}
		container.getNbt().put("BlockStateTag", blockStates);
	}
	
}
