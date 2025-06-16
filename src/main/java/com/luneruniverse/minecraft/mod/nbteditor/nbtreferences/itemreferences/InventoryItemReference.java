package com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.item.ItemStack;

public class InventoryItemReference implements ItemReference {
	
	private final int slot;
	private Runnable parent;
	
	/**
	 * @param slot Format: inv
	 */
	public InventoryItemReference(int slot) {
		this.slot = slot;
		this.parent = NBTEditorClient.CURSOR_MANAGER::showRoot;
	}
	public InventoryItemReference setParent(Runnable parent) {
		this.parent = parent;
		return this;
	}
	
	public int getSlot() {
		return slot;
	}
	
	@Override
	public boolean exists() {
		return true;
	}
	
	@Override
	public ItemStack getItem() {
		return MainUtil.client.player.getInventory().getStack(slot);
	}
	
	@Override
	public void saveItem(ItemStack toSave, Runnable onFinished) {
		MainUtil.saveItem(slot, toSave);
		onFinished.run();
	}
	
	@Override
	public boolean isLocked() {
		return false;
	}
	
	@Override
	public boolean isLockable() {
		return false;
	}
	
	@Override
	public int getBlockedSlot() {
		return slot;
	}
	
	@Override
	public void showParent() {
		parent.run();
	}
	
}
