package com.luneruniverse.minecraft.mod.nbteditor.screens.containers;

import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;

import net.minecraft.item.ItemStack;

public class CursorHistoryScreen extends ClientHandledScreen {
	
	public static void show(List<ItemStack> items, List<Integer> lockedItems) {
		NBTEditorClient.CURSOR_MANAGER.showBranch(new CursorHistoryScreen(items, lockedItems));
	}
	
	private final LockedSlotsInfo lockedSlots;
	
	private CursorHistoryScreen(List<ItemStack> items, List<Integer> lockedItems) {
		super(new ClientScreenHandler(6), TextInst.translatable("nbteditor.container.title")
				.append(TextInst.translatable("nbteditor.get.lost_item.history")));
		
		for (int i = 0; i < handler.getInventory().size() && i < items.size(); i++)
			handler.getSlot(i).setStackNoCallbacks(items.get(i).copy());
		
		lockedSlots = LockedSlotsInfo.ALL_LOCKED.copy();
		lockedItems.forEach(lockedSlots::addContainerSlot);
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
