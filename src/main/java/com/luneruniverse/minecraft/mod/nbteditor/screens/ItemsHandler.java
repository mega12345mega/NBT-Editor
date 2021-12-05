package com.luneruniverse.minecraft.mod.nbteditor.screens;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

public class ItemsHandler extends GenericContainerScreenHandler {
	
	public ItemsHandler(int syncId, PlayerInventory playerInventory) {
		super(ScreenHandlerType.GENERIC_9X3, syncId, playerInventory, new SimpleInventory(27), 3);
	}
	
}
