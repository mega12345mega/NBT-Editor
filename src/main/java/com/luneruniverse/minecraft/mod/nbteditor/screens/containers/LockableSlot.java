package com.luneruniverse.minecraft.mod.nbteditor.screens.containers;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class LockableSlot extends Slot {
	
	private static final Set<Thread> UNLOCKED_THREADS = Collections.synchronizedSet(new HashSet<>());
	public static void unlockDuring(Runnable callback) {
		UNLOCKED_THREADS.add(Thread.currentThread());
		try {
			callback.run();
		} finally {
			UNLOCKED_THREADS.remove(Thread.currentThread());
		}
	}
	
	public LockableSlot(Slot slot) {
		super(slot.inventory, slot.getIndex(), slot.x, slot.y);
		this.id = slot.id;
	}
	
	private boolean isBlocked() {
		if (UNLOCKED_THREADS.contains(Thread.currentThread()))
			return false;
		
		if (MainUtil.client.currentScreen instanceof ClientHandledScreen clientHandledScreen) {
			if (clientHandledScreen.getLockedSlotsInfo().isBlocked(this, false))
				return true;
		}
		return false;
	}
	
	@Override
	public boolean canInsert(ItemStack stack) {
		if (isBlocked())
			return false;
		return super.canInsert(stack);
	}
	
	@Override
	public boolean canTakeItems(PlayerEntity playerEntity) {
		if (isBlocked())
			return false;
		return super.canTakeItems(playerEntity);
	}
	
	// Prevent quick moving identical items into slot
	@Override
	public int getMaxItemCount() {
		if (isBlocked())
			return getStack().getCount();
		return super.getMaxItemCount();
	}
	
}
