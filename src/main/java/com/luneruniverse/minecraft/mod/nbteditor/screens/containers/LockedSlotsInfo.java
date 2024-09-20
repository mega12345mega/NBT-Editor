package com.luneruniverse.minecraft.mod.nbteditor.screens.containers;

import java.util.ArrayList;
import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public class LockedSlotsInfo {
	
	public static final LockedSlotsInfo NONE = new LockedSlotsInfo(false, false, true);
	public static final LockedSlotsInfo ITEMS_LOCKED = new LockedSlotsInfo(true, false, true);
	public static final LockedSlotsInfo ALL_LOCKED = new LockedSlotsInfo(true, true, true);
	
	private final boolean itemsLocked;
	private final boolean airLocked;
	private final boolean copyLockedItem;
	private final List<Integer> playerLockedSlots;
	private final List<Integer> playerLockedHotbarSlots;
	private final List<Integer> containerLockedSlots;
	
	public LockedSlotsInfo(boolean itemsLocked, boolean airLocked, boolean copyLockedItem) {
		this.itemsLocked = itemsLocked;
		this.airLocked = airLocked;
		this.copyLockedItem = copyLockedItem;
		this.playerLockedSlots = new ArrayList<>();
		this.playerLockedHotbarSlots = new ArrayList<>();
		this.containerLockedSlots = new ArrayList<>();
	}
	private LockedSlotsInfo(LockedSlotsInfo info) {
		this.itemsLocked = info.itemsLocked;
		this.airLocked = info.airLocked;
		this.copyLockedItem = info.copyLockedItem;
		this.playerLockedSlots = new ArrayList<>(info.playerLockedSlots);
		this.playerLockedHotbarSlots = new ArrayList<>(info.playerLockedHotbarSlots);
		this.containerLockedSlots = new ArrayList<>(info.containerLockedSlots);
	}
	
	public boolean isCopyLockedItem() {
		return copyLockedItem;
	}
	
	/**
	 * @param slot 0-26 inv, 27-35 hotbar, 40 offhand
	 */
	public LockedSlotsInfo addPlayerSlot(int slot) {
		if (slot == 40) {
			playerLockedHotbarSlots.add(40);
			return this;
		}
		playerLockedSlots.add(slot);
		if (slot >= 27)
			playerLockedHotbarSlots.add(slot - 27);
		return this;
	}
	public LockedSlotsInfo addPlayerSlot(ItemReference itemRef) {
		int slot = itemRef.getBlockedInvSlot();
		if (slot != -1)
			playerLockedSlots.add(slot);
		int hotbarSlot = itemRef.getBlockedHotbarSlot();
		if (hotbarSlot != -1)
			playerLockedHotbarSlots.add(hotbarSlot);
		return this;
	}
	
	public LockedSlotsInfo addContainerSlot(int slot) {
		containerLockedSlots.add(slot);
		return this;
	}
	
	public boolean isBlocked(Slot slot, int button, SlotActionType actionType, boolean explicitly) {
		if (actionType == SlotActionType.SWAP && playerLockedHotbarSlots.contains(button))
			return true;
		
		if (slot.inventory == MainUtil.client.player.getInventory()) {
			if (playerLockedSlots.contains(slot.getIndex() < 9 ? slot.getIndex() + 27 : slot.getIndex() - 9))
				return true;
		} else {
			if (containerLockedSlots.contains(slot.getIndex()))
				return true;
			
			if (!explicitly) {
				ItemStack item = slot.getStack();
				if (item == null || item.isEmpty()) {
					if (airLocked)
						return true;
				} else {
					if (itemsLocked)
						return true;
				}
			}
		}
		
		return false;
	}
	public boolean isBlocked(Slot slot, boolean explicitly) {
		return isBlocked(slot, 0, SlotActionType.PICKUP, explicitly);
	}
	
	public void renderLockedHighlights(MatrixStack matrices, ScreenHandler handler, boolean explicitly, boolean player, boolean container) {
		for (Slot slot : handler.slots) {
			if ((slot.inventory == MainUtil.client.player.getInventory() ? player : container) && isBlocked(slot, explicitly))
				MVDrawableHelper.drawSlotHighlight(matrices, slot.x, slot.y, 0x60FF0000);
		}
	}
	
	public LockedSlotsInfo copy() {
		return new LockedSlotsInfo(this);
	}
	
}
