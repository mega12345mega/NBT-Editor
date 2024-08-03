package com.luneruniverse.minecraft.mod.nbteditor.screens.containers;

import java.util.List;
import java.util.Optional;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public class CursorHistoryScreen extends ClientHandledScreen {
	
	private ItemStack[] prevInv;
	
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
	protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
		prevInv = new ItemStack[this.handler.getInventory().size()];
		for (int i = 0; i < prevInv.length; i++)
			prevInv[i] = this.handler.getInventory().getStack(i).copy();
		
		super.onMouseClick(slot, slotId, button, actionType);
	}
	
	@Override
	public SlotLockType getSlotLockType() {
		return SlotLockType.ALL_LOCKED;
	}
	@Override
	public ItemStack[] getPrevInventory() {
		return prevInv;
	}
	
	@Override
	public boolean shouldPause() {
		return true;
	}
	
}
