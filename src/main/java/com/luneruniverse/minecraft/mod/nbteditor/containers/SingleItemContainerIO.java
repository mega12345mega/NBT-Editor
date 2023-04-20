package com.luneruniverse.minecraft.mod.nbteditor.containers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class SingleItemContainerIO extends MultiTargetContainerIO {
	
	private final String key;
	
	public SingleItemContainerIO(Target target, String key) {
		super(target);
		this.key = key;
	}
	
	@Override
	public ItemStack[] readItems(ItemStack container) {
		NbtCompound item = target.getItemsParent(container).getCompound(key);
		if (item.isEmpty())
			return new ItemStack[] {ItemStack.EMPTY};
		return new ItemStack[] {ItemStack.fromNbt(item)};
	}
	
	@Override
	public void writeItems(ItemStack container, ItemStack[] contents) {
		NbtCompound tag = target.getItemsParent(container);
		if (contents[0] == null || contents[0].isEmpty())
			tag.remove(key);
		else
			tag.put(key, contents[0].writeNbt(new NbtCompound()));
	}
	
}
