package com.luneruniverse.minecraft.mod.nbteditor.screens.containers;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

public class CursorHistoryHandler extends GenericContainerScreenHandler {
	
	public CursorHistoryHandler(int syncId, PlayerInventory playerInventory) {
		super(ScreenHandlerType.GENERIC_9X6, syncId, playerInventory, new SimpleInventory(54), 6);
	}
	
}
