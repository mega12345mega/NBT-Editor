package com.luneruniverse.minecraft.mod.nbteditor.screens.containers;

import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;

import net.minecraft.item.ItemStack;

public class CursorHistoryScreen extends ClientHandledScreen {
	
	private final LockedSlotsInfo lockedSlots;
	
	private CursorHistoryScreen(CursorHistoryHandler handler) {
		super(handler, TextInst.translatable("nbteditor.container.title").append(TextInst.translatable("nbteditor.get.lost_item.history")));
		lockedSlots = LockedSlotsInfo.ALL_LOCKED.copy();
	}
	private CursorHistoryScreen build(List<ItemStack> items, List<Integer> lockedItems) {
		for (int i = 0; i < this.handler.getInventory().size(); i++) {
			if (i == items.size())
				break;
			this.handler.getSlot(i).setStackNoCallbacks(items.get(i));
		}
		
		lockedItems.forEach(lockedSlots::addContainerSlot);
		
		return this;
	}
	public static void show(List<ItemStack> items, List<Integer> lockedItems) {
		NBTEditorClient.CURSOR_MANAGER.showBranch(new CursorHistoryScreen(new CursorHistoryHandler()).build(items, lockedItems));
	}
	
	@Override
	public LockedSlotsInfo getLockedSlotsInfo() {
		return lockedSlots;
	}
	
	@Override
	public boolean shouldPause() {
		return true;
	}
	
}
