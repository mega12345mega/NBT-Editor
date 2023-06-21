package com.luneruniverse.minecraft.mod.nbteditor.itemreferences;

import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.ItemStack;

public class InventoryItemReference implements ItemReference {
	
	private final int slot;
	
	public InventoryItemReference(int slot) {
		this.slot = slot;
	}
	
	public int getSlot() {
		return slot;
	}
	
	@Override
	public ItemStack getItem() {
		if (slot == 45)
			return MainUtil.client.player.getOffHandStack();
		return MainUtil.client.player.getInventory().getStack(slot);
	}
	
	@Override
	public void saveItem(ItemStack toSave, Runnable onFinished) {
		MainUtil.saveItemInvSlot(slot, toSave);
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
	public int getBlockedInvSlot() {
		if (slot == 45)
			return -1;
		return slot < 9 ? slot + 27 : slot - 9;
	}
	
	@Override
	public int getBlockedHotbarSlot() {
		if (slot == 45)
			return 40;
		if (slot < 9)
			return slot;
		return -1;
	}
	
	@Override
	public void showParent() {
		MainUtil.client.setScreen(new InventoryScreen(MainUtil.client.player));
	}
	
}
