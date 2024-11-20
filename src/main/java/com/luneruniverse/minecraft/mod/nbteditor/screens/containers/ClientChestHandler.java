package com.luneruniverse.minecraft.mod.nbteditor.screens.containers;

import com.luneruniverse.minecraft.mod.nbteditor.clientchest.ClientChestPage;

import net.minecraft.item.ItemStack;

public class ClientChestHandler extends ClientScreenHandler {
	
	public ClientChestHandler(ClientChestPage pageData) {
		super(6);
		fillPage(pageData);
	}
	
	public void fillPage(ClientChestPage pageData) {
		ItemStack[] items = pageData.getItemsOrThrow();
		for (int i = 0; i < items.length; i++)
			getSlot(i).setStackNoCallbacks(items[i] == null ? ItemStack.EMPTY : items[i].copy());
	}
	
}
