package com.luneruniverse.minecraft.mod.nbteditor.screens.containers;

import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class LockableSlot extends Slot {
	
	public LockableSlot(Slot slot) {
		super(slot.inventory, slot.getIndex(), slot.x, slot.y);
		this.id = slot.id;
	}
	
	@Override
	public boolean canInsert(ItemStack stack) {
		if (MainUtil.client.currentScreen instanceof ClientHandledScreen clientHandledScreen) {
			if (clientHandledScreen.getLockedSlotsInfo().isBlocked(this, false))
				return false;
		}
		return super.canInsert(stack);
	}
	
	@Override
	public boolean canTakeItems(PlayerEntity playerEntity) {
		if (MainUtil.client.currentScreen instanceof ClientHandledScreen clientHandledScreen) {
			if (clientHandledScreen.getLockedSlotsInfo().isBlocked(this, false))
				return false;
		}
		return super.canTakeItems(playerEntity);
	}
	
	// Prevent quick moving identical items into slot
	@Override
	public int getMaxItemCount() {
		if (MainUtil.client.currentScreen instanceof ClientHandledScreen clientHandledScreen) {
			if (clientHandledScreen.getLockedSlotsInfo().isBlocked(this, false))
				return getStack().getCount();
		}
		return super.getMaxItemCount();
	}
	
}
