package com.luneruniverse.minecraft.mod.nbteditor.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

public class ItemChest {
	
	public static <T> ItemStack writeDatabase(ItemStack container, List<T> data, Function<T, ItemStack> stackGenerator, List<String> path) {
		ItemChest chest = new ItemChest(container);
		if (data.size() <= 27) {
			for (T item : data)
				chest.addStack(stackGenerator.apply(item));
		} else {
			int sectionSize = 27;
			while (data.size() / sectionSize > 27)
				sectionSize *= 27;
			for (int i = 0; i < 27; i++) {
				if (i * sectionSize >= data.size())
					break;
				
				ItemStack section = new ItemStack(Items.SHULKER_BOX);
				path.add(i + "");
				section.setCustomName(TextInst.of(TextInst.translatable("nbteditor.hdb.section").getString() + ": " + String.join(".", path)));
				writeDatabase(section, data.subList(i * sectionSize, Math.min(data.size(), (i + 1) * sectionSize)), stackGenerator, path);
				path.remove(path.size() - 1);
				chest.addStack(section);
			}
		}
		
		return container;
	}
	public static <T> ItemStack writeDatabase(ItemStack container, List<T> data, Function<T, ItemStack> stackGenerater) {
		return writeDatabase(container, data, stackGenerater, new ArrayList<>());
	}
	
	
	
	private final ItemStack item;
	private final SimpleInventory inv;
	
	public ItemChest(ItemStack item) {
		
		this.item = item;
		this.inv = new SimpleInventory(27);
		
		if (item.hasNbt()) {
			NbtCompound nbt = item.getNbt();
			if (nbt.contains("BlockEntityTag", NbtElement.COMPOUND_TYPE)) {
				NbtCompound blockEntityTag = nbt.getCompound("BlockEntityTag");
				if (blockEntityTag.contains("Items", NbtElement.LIST_TYPE)) {
					NbtList items = blockEntityTag.getList("Items", NbtElement.COMPOUND_TYPE);
					if (!items.isEmpty()) {
						for (NbtElement containedItemElement : items) {
							NbtCompound containedItem = (NbtCompound) containedItemElement;
							try {
								inv.setStack(containedItem.getByte("Slot"), ItemStack.fromNbt(containedItem));
							} catch (Exception e) {
								NBTEditor.LOGGER.error("Error while reading a container", e);
							}
						}
					}
				}
			}
		}
		
	}
	
	public ItemStack getItem() {
		return item;
	}
	
	
	public boolean addStack(ItemStack item) {
		if (inv.addStack(item) == ItemStack.EMPTY) {
			save();
			return true;
		}
		
		return false;
	}
	public boolean addStack(ItemChest item) {
		return addStack(item.getItem());
	}
	
	public void setStack(int slot, ItemStack item) {
		inv.setStack(slot, item);
		save();
	}
	public void setStack(int slot, ItemChest item) {
		setStack(slot, item.getItem());
	}
	public void setAll(ItemStack[] items) {
		for (int i = 0; i < items.length; i++)
			inv.setStack(i, items[i]);
		save();
	}
	
	public ItemStack getStack(int slot) {
		return inv.getStack(slot);
	}
	public ItemStack[] getAll() {
		ItemStack[] output = new ItemStack[inv.size()];
		for (int i = 0; i < output.length; i++)
			output[i] = inv.getStack(i);
		return output;
	}
	
	private void save() {
		NbtCompound nbt = item.getOrCreateNbt();
		if (!nbt.contains("BlockEntityTag", NbtElement.COMPOUND_TYPE))
			nbt.put("BlockEntityTag", new NbtCompound());
		NbtCompound blockEntityTag = nbt.getCompound("BlockEntityTag");
		NbtList items = blockEntityTag.getList("Items", NbtElement.COMPOUND_TYPE);
		blockEntityTag.put("Items", items);
		
		items.clear();
		for (int i = 0; i < inv.size(); i++) {
			ItemStack item = inv.getStack(i);
			if (item == null || item.isEmpty())
				continue;
			NbtCompound itemNbt = new NbtCompound();
			itemNbt.putByte("Slot", (byte) i);
			item.writeNbt(itemNbt);
			items.add(itemNbt);
		}
	}
	
}
