package com.luneruniverse.minecraft.mod.nbteditor.screens.containers;

import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.luneruniverse.minecraft.mod.nbteditor.util.SlotUtil;

import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.SetPlayerInventoryS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class ServerInventoryManager {
	
	private final Inventory serverInv;
	private boolean updatingServer;
	
	public ServerInventoryManager() {
		Inventory playerInv = MainUtil.client.player.getInventory();
		serverInv = new SimpleInventory(playerInv.size());
		for (int i = 0; i < serverInv.size(); i++)
			serverInv.setStack(i, playerInv.getStack(i).copy());
	}
	
	private ScreenHandler getScreenHandler(int syncId) {
		if (syncId == 0)
			return MainUtil.client.player.playerScreenHandler;
		if (syncId == MainUtil.client.player.currentScreenHandler.syncId)
			return MainUtil.client.player.currentScreenHandler;
		return null;
	}
	
	public void onSetPlayerInventoryPacket(SetPlayerInventoryS2CPacket packet) {
		serverInv.setStack(packet.slot(), packet.contents().copy());
	}
	
	public void onInventoryPacket(InventoryS2CPacket packet) {
		ScreenHandler handler = getScreenHandler(packet.getSyncId());
		if (handler == null)
			return;
		
		for (int i = 0; i < packet.getContents().size(); i++) {
			Slot slot = handler.getSlot(i);
			if (slot.inventory == MainUtil.client.player.getInventory())
				serverInv.setStack(slot.getIndex(), packet.getContents().get(i).copy());
		}
	}
	
	public void onScreenHandlerSlotUpdatePacket(ScreenHandlerSlotUpdateS2CPacket packet) {
		if (packet.getSyncId() == -1)
			return;
		
		if (packet.getSyncId() == -2) {
			serverInv.setStack(packet.getSlot(), packet.getStack().copy());
			return;
		}
		
		ScreenHandler handler = getScreenHandler(packet.getSyncId());
		if (handler == null)
			return;
		Slot slot = handler.getSlot(packet.getSlot());
		if (slot.inventory == MainUtil.client.player.getInventory())
			serverInv.setStack(slot.getIndex(), packet.getStack().copy());
	}
	
	public void updateServer() {
		try {
			updatingServer = true;
			
			Inventory playerInv = MainUtil.client.player.getInventory();
			for (int i = 0; i < serverInv.size(); i++) {
				ItemStack item = playerInv.getStack(i);
				if (!ItemStack.areEqual(item, serverInv.getStack(i))) {
					MainUtil.clickCreativeStack(item, SlotUtil.invToContainer(i));
					serverInv.setStack(i, item.copy());
				}
			}
		} finally {
			updatingServer = false;
		}
	}
	
	public boolean isUpdatingServer() {
		return updatingServer;
	}
	
}
