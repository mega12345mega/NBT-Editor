package com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVClientNetworking;
import com.luneruniverse.minecraft.mod.nbteditor.packets.SetSlotC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;

public class ServerItemReference implements ItemReference {
	
	private final HandledScreen<?> screen;
	private final int slot;
	
	/**
	 * @param screen
	 * @param slot Format: generic container
	 */
	public ServerItemReference(HandledScreen<?> screen, int slot) {
		if (screen.getScreenHandler().getSlot(slot).inventory == MainUtil.client.player.getInventory())
			throw new IllegalArgumentException("The slot cannot be in the player's inventory!");
		
		this.screen = screen;
		this.slot = slot;
	}
	
	public int getSlot() {
		return slot;
	}
	
	@Override
	public boolean exists() {
		return NBTEditorClient.CURSOR_MANAGER.getCurrentRoot() == screen &&
				!NBTEditorClient.CURSOR_MANAGER.isCurrentRootClosed();
	}
	
	@Override
	public ItemStack getItem() {
		return screen.getScreenHandler().getSlot(slot).getStack();
	}
	
	@Override
	public void saveItem(ItemStack toSave, Runnable onFinished) {
		screen.getScreenHandler().getSlot(slot).setStackNoCallbacks(toSave);
		if (NBTEditorClient.SERVER_CONN.isContainerScreen())
			MVClientNetworking.send(new SetSlotC2SPacket(slot, toSave));
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
		return -1;
	}
	
	@Override
	public void showParent() {
		NBTEditorClient.CURSOR_MANAGER.showBranch(screen);
	}
	
}
