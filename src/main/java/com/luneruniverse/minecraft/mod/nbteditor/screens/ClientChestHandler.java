package com.luneruniverse.minecraft.mod.nbteditor.screens;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

public class ClientChestHandler extends GenericContainerScreenHandler {
	
	public ClientChestHandler(int syncId, PlayerInventory playerInventory) {
		super(ScreenHandlerType.GENERIC_9X6, syncId, playerInventory, new SimpleInventory(54), 6);
		fillPage();
	}
	
	public void fillPage() {
		ItemStack[] items = NBTEditorClient.getClientChestPage(ClientChestScreen.PAGE);
		for (int i = 0; i < items.length; i++)
			getSlot(i).setStack(items[i] == null ? ItemStack.EMPTY : items[i].copy());
	}
	
}
