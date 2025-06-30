package com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.luneruniverse.minecraft.mod.nbteditor.util.SlotUtil;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class HandItemReference implements ItemReference {
	
	private final Hand hand;
	
	public HandItemReference(Hand hand) {
		this.hand = hand;
	}
	
	public Hand getHand() {
		return hand;
	}
	
	@Override
	public boolean exists() {
		return true;
	}
	
	@Override
	public ItemStack getItem() {
		return MainUtil.client.player.getStackInHand(hand);
	}
	
	@Override
	public void saveItem(ItemStack toSave, Runnable onFinished) {
		MainUtil.saveItem(hand, toSave);
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
		if (hand == Hand.MAIN_HAND)
			return SlotUtil.createHotbarInInv(MainUtil.client.player.getInventory().selectedSlot);
		return SlotUtil.createOffHandInInv();
	}
	
	@Override
	public void showParent() {
		NBTEditorClient.CURSOR_MANAGER.closeRoot();
	}
	
}
