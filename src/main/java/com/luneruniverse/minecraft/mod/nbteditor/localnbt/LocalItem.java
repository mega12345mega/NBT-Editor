package com.luneruniverse.minecraft.mod.nbteditor.localnbt;

import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public abstract class LocalItem implements LocalNBT {
	public abstract LocalItemStack toStack();
	public abstract LocalItemParts toParts();
	
	/**
	 * Will throw if called on a {@link LocalItemParts}
	 * @see #getReadableItem()
	 * @see #toItem()
	 */
	public abstract ItemStack getEditableItem();
	/**
	 * Will not throw if called on a {@link LocalItemParts}
	 * @see #getEditableItem()
	 * @see #toItem()
	 */
	public abstract ItemStack getReadableItem();
	
	public abstract Item getItemType();
	
	public abstract int getCount();
	public abstract void setCount(int count);
	
	public boolean receive() {
		ItemStack item = getReadableItem();
		if (item.isEmpty())
			return false;
		MainUtil.getWithMessage(item);
		return true;
	}
	
	@Override
	public boolean equals(Object nbt) {
		if (nbt instanceof LocalItem item)
			return ItemStack.areEqual(this.getReadableItem(), item.getReadableItem());
		return false;
	}
}
