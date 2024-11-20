package com.luneruniverse.minecraft.mod.nbteditor.util;

import java.util.WeakHashMap;

import com.luneruniverse.minecraft.mod.nbteditor.clientchest.DynamicItems;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVClientNetworking;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class SingleDynamicItem {
	
	private static final int SLOT = 0;
	private static final WeakHashMap<SingleDynamicItem, Boolean> SINGLE_DYNAMIC_ITEMS = new WeakHashMap<>();
	static {
		MVClientNetworking.PlayNetworkStateEvents.Start.EVENT.register(networkHandler ->
				SINGLE_DYNAMIC_ITEMS.keySet().forEach(SingleDynamicItem::tryLoad));
	}
	
	private final DynamicItems items;
	private ItemStack item;
	
	public SingleDynamicItem(ItemStack item) {
		this.items = new DynamicItems();
		this.item = this.items.tryAdd(SLOT, item);
		
		SINGLE_DYNAMIC_ITEMS.put(this, true);
	}
	public SingleDynamicItem(NbtCompound nbt) {
		this.items = new DynamicItems();
		this.items.add(SLOT, nbt, false);
		this.item = this.items.tryLoad(SLOT);
		
		SINGLE_DYNAMIC_ITEMS.put(this, true);
	}
	
	public synchronized ItemStack getItem() {
		return item;
	}
	public synchronized NbtCompound getOriginalNbt() {
		if (items.isSlot(SLOT))
			return items.getOriginalNbt(SLOT);
		return item.manager$serialize(true);
	}
	
	public synchronized boolean isDynamic() {
		return items.isSlot(SLOT);
	}
	public synchronized boolean isLocked() {
		return items.isSlotLocked(SLOT);
	}
	
	private synchronized void tryLoad() {
		if (items.isSlot(SLOT))
			item = items.tryLoad(SLOT);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SingleDynamicItem dynamicItem)
			return getOriginalNbt().equals(dynamicItem.getOriginalNbt());
		
		return false;
	}
	
}
