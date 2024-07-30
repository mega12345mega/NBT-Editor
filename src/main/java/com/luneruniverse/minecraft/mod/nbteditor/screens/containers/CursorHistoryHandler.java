package com.luneruniverse.minecraft.mod.nbteditor.screens.containers;

import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

public class CursorHistoryHandler extends GenericContainerScreenHandler {
	
	public CursorHistoryHandler() {
		super(ScreenHandlerType.GENERIC_9X6, ClientHandledScreen.SYNC_ID, MainUtil.client.player.getInventory(), new SimpleInventory(54), 6);
	}
	
}
