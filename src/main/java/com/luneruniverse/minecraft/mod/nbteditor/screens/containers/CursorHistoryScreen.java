package com.luneruniverse.minecraft.mod.nbteditor.screens.containers;

import java.util.List;
import java.util.Optional;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

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
	public static void show(List<ItemStack> items, List<Integer> lockedItems, Optional<ItemStack> cursor) {
		CursorHistoryHandler handler = new CursorHistoryHandler();
		handler.setCursorStack(cursor.orElse(MainUtil.client.player.playerScreenHandler.getCursorStack()));
		MainUtil.client.setScreen(new CursorHistoryScreen(handler).build(items, lockedItems));
	}
	public static void show(List<ItemStack> items, List<Integer> lockedItems) {
		show(items, lockedItems, Optional.empty());
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
