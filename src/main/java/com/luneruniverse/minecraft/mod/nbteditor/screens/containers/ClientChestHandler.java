package com.luneruniverse.minecraft.mod.nbteditor.screens.containers;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

public class ClientChestHandler extends GenericContainerScreenHandler {
	
	public ClientChestHandler() {
		super(ScreenHandlerType.GENERIC_9X6, ClientHandledScreen.SYNC_ID, MainUtil.client.player.getInventory(), new SimpleInventory(54), 6);
		fillPage();
	}
	
	public void fillPage() {
		ItemStack[] items = NBTEditorClient.CLIENT_CHEST.getPage(ClientChestScreen.PAGE).getItemsOrThrow();
		for (int i = 0; i < items.length; i++)
			getSlot(i).setStackNoCallbacks(items[i] == null ? ItemStack.EMPTY : items[i].copy());
	}
	
}
