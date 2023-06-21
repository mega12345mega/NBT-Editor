package com.luneruniverse.minecraft.mod.nbteditor.screens.containers;

import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

public class CursorHistoryHandler extends GenericContainerScreenHandler {
	
	public CursorHistoryHandler(int syncId, PlayerInventory playerInventory) {
		super(ScreenHandlerType.GENERIC_9X6, syncId, playerInventory, new SimpleInventory(54), 6);
	}
	
	
	@Override
	public void setCursorStack(ItemStack stack) {
		MainUtil.client.player.playerScreenHandler.setCursorStack(stack);
	}
	@Override
	public ItemStack getCursorStack() {
		return MainUtil.client.player.playerScreenHandler.getCursorStack();
	}
	
}
