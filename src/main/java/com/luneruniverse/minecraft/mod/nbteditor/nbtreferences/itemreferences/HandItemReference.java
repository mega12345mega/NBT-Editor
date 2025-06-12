package com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences;

import java.util.Optional;

import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
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
	public int getBlockedInvSlot() {
		if (hand == Hand.MAIN_HAND)
			return MainUtil.client.player.getInventory().selectedSlot + 27;
		return -1;
	}
	
	@Override
	public int getBlockedHotbarSlot() {
		if (hand == Hand.MAIN_HAND)
			return MainUtil.client.player.getInventory().selectedSlot;
		return 40;
	}
	
	@Override
	public void showParent(Optional<ItemStack> cursor) {
		HandledScreen<?> newScreen = new InventoryScreen(MainUtil.client.player);
		cursor.ifPresent(value -> MainUtil.setRootCursorStack(newScreen.getScreenHandler(), value));
		MainUtil.client.player.currentScreenHandler = newScreen.getScreenHandler();
		MainUtil.client.setScreen(newScreen);
	}
	
	@Override
	public void clearParentCursor() {
		MainUtil.setRootCursorStack(MainUtil.client.player.playerScreenHandler, ItemStack.EMPTY);
	}
	
}
