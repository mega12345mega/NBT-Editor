package com.luneruniverse.minecraft.mod.nbteditor.clientchest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.DynamicRegistryManagerHolder;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.Attempt;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.NBTManagers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class DynamicItems {
	
	private final Map<Integer, Map.Entry<NbtCompound, Boolean>> items;
	
	public DynamicItems() {
		this.items = new HashMap<>();
	}
	private DynamicItems(Map<Integer, Map.Entry<NbtCompound, Boolean>> items) {
		this.items = items;
	}
	
	public void add(int slot, NbtCompound nbt, boolean successfullyLoaded) {
		items.put(slot, Map.entry(nbt, successfullyLoaded));
	}
	public ItemStack tryAdd(int slot, ItemStack item) {
		if (item.isEmpty())
			return ItemStack.EMPTY;
		
		NbtCompound nbt = NBTManagers.ITEM.serialize(item, true);
		Attempt<ItemStack> defaultRegistryItem = DynamicRegistryManagerHolder
				.withDefaultManager(() -> NBTManagers.ITEM.tryDeserialize(nbt));
		
		if (defaultRegistryItem.isSuccessful())
			return defaultRegistryItem.getSuccessOrThrow();
		
		add(slot, nbt, true);
		return item;
	}
	public void remove(int slot) {
		items.remove(slot);
	}
	
	public ItemStack tryLoad(int slot) {
		Map.Entry<NbtCompound, Boolean> item = items.get(slot);
		if (item == null)
			throw new IllegalArgumentException("Not a dynamic slot: " + slot);
		NbtCompound nbt = item.getKey();
		
		Attempt<ItemStack> attempt = NBTManagers.ITEM.tryDeserialize(nbt);
		items.put(slot, Map.entry(nbt, attempt.isSuccessful()));
		return attempt.value().orElse(ItemStack.EMPTY);
	}
	
	public void unloadAll() {
		items.replaceAll((slot, entry) -> Map.entry(entry.getKey(), false));
	}
	
	public List<Integer> getSlots() {
		return items.keySet().stream().toList();
	}
	public boolean isSlot(int slot) {
		return items.containsKey(slot);
	}
	
	public List<Integer> getLockedSlots() {
		return items.entrySet().stream().filter(entry -> !entry.getValue().getValue()).map(Map.Entry::getKey).toList();
	}
	public boolean isSlotLocked(int slot) {
		Map.Entry<NbtCompound, Boolean> item = items.get(slot);
		return item != null && !item.getValue();
	}
	
	public NbtCompound getOriginalNbt(int slot) {
		Map.Entry<NbtCompound, Boolean> item = items.get(slot);
		return item == null ? null : item.getKey();
	}
	
	public DynamicItems copy() {
		return new DynamicItems(new HashMap<>(items));
	}
	
}
