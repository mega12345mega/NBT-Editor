package com.luneruniverse.minecraft.mod.nbteditor.containers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

public class ConstSizeContainerIO extends MultiTargetContainerIO {
	
	private final int numItems;
	
	public ConstSizeContainerIO(Target target, int numItems) {
		super(target);
		this.numItems = numItems;
	}
	
	@Override
	public ItemStack[] readItems(ItemStack container) {
		ItemStack[] output = new ItemStack[numItems];
		NbtList items = target.getItemsParent(container).getList("Items", NbtElement.COMPOUND_TYPE);
		for (NbtElement item : items)
			output[((NbtCompound) item).getInt("Slot")] = ItemStack.fromNbt((NbtCompound) item);
		return output;
	}
	
	@Override
	public void writeItems(ItemStack container, ItemStack[] contents) {
		NbtList items = new NbtList();
		for (int i = 0; i < numItems; i++) {
			ItemStack item = (contents[i] == null ? ItemStack.EMPTY : contents[i]);
			if (!item.isEmpty()) {
				NbtCompound nbt = new NbtCompound();
				nbt.putInt("Slot", i);
				items.add(item.writeNbt(nbt));
			}
		}
		target.getItemsParent(container).put("Items", items);
	}
	
}
