package com.luneruniverse.minecraft.mod.nbteditor.screens.containers;

import java.util.List;
import java.util.Optional;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.item.ItemStack;

public class CursorHistoryScreen extends ClientHandledScreen {
	
	private CursorHistoryScreen(CursorHistoryHandler handler) {
		super(handler, TextInst.translatable("nbteditor.container.title").append(TextInst.translatable("nbteditor.get.lost_item.history")));
	}
	private CursorHistoryScreen build(List<ItemStack> items) {
		for (int i = 0; i < this.handler.getInventory().size(); i++) {
			if (i == items.size())
				break;
			this.handler.getSlot(i).setStackNoCallbacks(items.get(i).copy());
		}
		
		return this;
	}
	public static void show(List<ItemStack> items, Optional<ItemStack> cursor) {
		CursorHistoryHandler handler = new CursorHistoryHandler();
		handler.setCursorStack(cursor.orElse(MainUtil.client.player.playerScreenHandler.getCursorStack()));
		MainUtil.client.setScreen(new CursorHistoryScreen(handler).build(items));
	}
	public static void show(List<ItemStack> items) {
		show(items, Optional.empty());
	}
	
	@Override
	public LockedSlotsInfo getLockedSlotsInfo() {
		return LockedSlotsInfo.ALL_LOCKED;
	}
	
	@Override
	public boolean shouldPause() {
		return true;
	}
	
}
