package com.luneruniverse.minecraft.mod.nbteditor.screens.containers;

import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

public class ClientScreenHandler extends GenericContainerScreenHandler {
	
	public static final int SYNC_ID = -2718;
	
	public ClientScreenHandler(int rows) {
		super(switch (rows) {
			case 1 -> ScreenHandlerType.GENERIC_9X1;
			case 2 -> ScreenHandlerType.GENERIC_9X2;
			case 3 -> ScreenHandlerType.GENERIC_9X3;
			case 4 -> ScreenHandlerType.GENERIC_9X4;
			case 5 -> ScreenHandlerType.GENERIC_9X5;
			case 6 -> ScreenHandlerType.GENERIC_9X6;
			default -> throw new IllegalArgumentException("Invalid row count: " + rows);
		}, SYNC_ID, MainUtil.client.player.getInventory(), new SimpleInventory(rows * 9), rows);
		
		slots.replaceAll(LockableSlot::new);
	}
	
}
