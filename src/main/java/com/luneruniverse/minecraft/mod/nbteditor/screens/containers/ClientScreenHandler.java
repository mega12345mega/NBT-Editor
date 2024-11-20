package com.luneruniverse.minecraft.mod.nbteditor.screens.containers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;

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
	
	@Override
	protected boolean insertItem(ItemStack stack, int startIndex, int endIndex, boolean fromLast) {
		boolean changed = false;
		List<Integer> indices = IntStream.range(startIndex, endIndex).collect(ArrayList::new, List::add, List::addAll);
		if (fromLast)
			Collections.reverse(indices);
		
		if (stack.isStackable()) {
			for (int i : indices) {
				if (stack.isEmpty())
					break;
				Slot slot = slots.get(i);
				ItemStack slotStack = slot.getStack();
				
				if (!slotStack.isEmpty() && ItemStack.areItemsAndComponentsEqual(stack, slotStack)) {
					int total = slotStack.getCount() + stack.getCount();
					int max = slot.getMaxItemCount(slotStack);
					if (total <= max) {
						stack.setCount(0);
						slotStack.setCount(total);
						slot.markDirty();
						changed = true;
					} else if (slotStack.getCount() < max) {
						stack.decrement(max - slotStack.getCount());
						slotStack.setCount(max);
						slot.markDirty();
						changed = true;
					}
				}
			}
		}
		
		if (!stack.isEmpty()) {
			for (int i : indices) {
				Slot slot = slots.get(i);
				ItemStack slotStack = slot.getStack();
				
				if (slotStack.isEmpty() && slot.canInsert(stack)) {
					int max = slot.getMaxItemCount(stack);
					slot.setStackNoCallbacks(stack.split(Math.min(stack.getCount(), max)));
					slot.markDirty();
					changed = true;
					break;
				}
			}
		}
		
		return changed;
	}
	
}
