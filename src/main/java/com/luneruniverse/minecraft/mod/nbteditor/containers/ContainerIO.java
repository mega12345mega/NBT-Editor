package com.luneruniverse.minecraft.mod.nbteditor.containers;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.registry.Registry;

public abstract class ContainerIO {
	
	public static final ContainerIO CHEST = new ChestIO();
	public static final ContainerIO ENTITY = new EntityIO();
	public static final ContainerIO FURNACE = new ItemsContainerIO(false, 3);
	public static final ContainerIO BREWING_STAND = new ItemsContainerIO(false, 5);
	public static final ContainerIO CAMPFIRE = new ItemsContainerIO(false, 4);
	public static final ContainerIO DISPENSER = new ItemsContainerIO(false, 9);
	public static final ContainerIO HOPPER = new ItemsContainerIO(false, 5);
	public static final ContainerIO JUKEBOX = new SingleItemContainerIO(false, "RecordItem");
	public static final ContainerIO LECTERN = new SingleItemContainerIO(false, "Book");
	public static final ContainerIO ITEM_FRAME = new SingleItemContainerIO(true, "Item");
	
	private static final Map<Item, ContainerIO> CONTAINERS;
	static {
		CONTAINERS = new HashMap<>();
		
		// Main containers
		for (Item shulkerBox : Registry.ITEM) {
			if (shulkerBox instanceof BlockItem block && block.getBlock() instanceof ShulkerBoxBlock)
				CONTAINERS.put(shulkerBox, CHEST);
		}
		CONTAINERS.put(Items.CHEST, CHEST);
		CONTAINERS.put(Items.TRAPPED_CHEST, CHEST);
		CONTAINERS.put(Items.BARREL, CHEST);
		
		// Other containers
		CONTAINERS.put(Items.ARMOR_STAND, ENTITY);
		for (Item spawnEgg : Registry.ITEM) {
			if (spawnEgg instanceof SpawnEggItem)
				CONTAINERS.put(spawnEgg, ENTITY);
		}
		
		CONTAINERS.put(Items.FURNACE, FURNACE);
		CONTAINERS.put(Items.BLAST_FURNACE, FURNACE);
		CONTAINERS.put(Items.SMOKER, FURNACE);
		
		CONTAINERS.put(Items.BREWING_STAND, BREWING_STAND);
		CONTAINERS.put(Items.CAMPFIRE, CAMPFIRE);
		CONTAINERS.put(Items.SOUL_CAMPFIRE, CAMPFIRE);
		CONTAINERS.put(Items.DISPENSER, DISPENSER);
		CONTAINERS.put(Items.DROPPER, DISPENSER);
		CONTAINERS.put(Items.HOPPER, HOPPER);
		CONTAINERS.put(Items.JUKEBOX, JUKEBOX);
		CONTAINERS.put(Items.LECTERN, LECTERN);
		CONTAINERS.put(Items.ITEM_FRAME, ITEM_FRAME);
		CONTAINERS.put(Items.GLOW_ITEM_FRAME, ITEM_FRAME);
	}
	
	public static boolean isContainer(ItemStack item) {
		return CONTAINERS.containsKey(item.getItem());
	}
	public static ItemStack[] read(ItemStack container) {
		ItemStack[] output = CONTAINERS.get(container.getItem()).readItems(container);
		for (int i = 0; i < output.length; i++) {
			if (output[i] == null)
				output[i] = ItemStack.EMPTY;
		}
		return output;
	}
	public static void write(ItemStack container, ItemStack[] contents) {
		CONTAINERS.get(container.getItem()).writeItems(container, contents);
	}
	
	
	
	public abstract ItemStack[] readItems(ItemStack container);
	public abstract void writeItems(ItemStack container, ItemStack[] contents);
	
}
