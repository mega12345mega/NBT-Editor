package com.luneruniverse.minecraft.mod.nbteditor.screens.containers;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;

import net.minecraft.item.ItemStack;

public class ClientChestHandler extends ClientScreenHandler {
	
	public ClientChestHandler() {
		super(6);
		fillPage();
	}
	
	public void fillPage() {
		ItemStack[] items = NBTEditorClient.CLIENT_CHEST.getPage(ClientChestScreen.PAGE).getItemsOrThrow();
		for (int i = 0; i < items.length; i++)
			getSlot(i).setStackNoCallbacks(items[i] == null ? ItemStack.EMPTY : items[i].copy());
	}
	
}
