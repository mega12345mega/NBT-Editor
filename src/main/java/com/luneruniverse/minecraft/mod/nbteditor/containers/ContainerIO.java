package com.luneruniverse.minecraft.mod.nbteditor.containers;

import java.util.HashMap;
import java.util.Map;

import com.luneruniverse.minecraft.mod.nbteditor.containers.MultiTargetContainerIO.Target;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;

public abstract class ContainerIO {
	
	public static final ContainerIO CHEST = new ChestIO();
	public static final ContainerIO ENTITY = new EntityIO();
	public static final ContainerIO FURNACE = new ConstSizeContainerIO(Target.BLOCK_ENTITY, 3);
	public static final ContainerIO BREWING_STAND = new ConstSizeContainerIO(Target.BLOCK_ENTITY, 5);
	public static final ContainerIO CAMPFIRE = new ConstSizeContainerIO(Target.BLOCK_ENTITY, 4);
	public static final ContainerIO DISPENSER = new ConstSizeContainerIO(Target.BLOCK_ENTITY, 9);
	public static final ContainerIO HOPPER = new ConstSizeContainerIO(Target.BLOCK_ENTITY, 5);
	public static final ContainerIO JUKEBOX = new SingleItemContainerIO(Target.BLOCK_ENTITY, "RecordItem");
	public static final ContainerIO LECTERN = new SingleItemContainerIO(Target.BLOCK_ENTITY, "Book");
	public static final ContainerIO ITEM_FRAME = new SingleItemContainerIO(Target.ENTITY, "Item");
	public static final ContainerIO BUNDLE = new DynamicSizeContainerIO(Target.ITEM);
	public static final ContainerIO CHISELED_BOOKSHELF = new ChiseledBookshelfContainerIO();
	public static final ContainerIO SUSPICIOUS_SAND = new SingleItemContainerIO(Target.BLOCK_ENTITY, "item");
	public static final ContainerIO SUSPICIOUS_GRAVEL = new SingleItemContainerIO(Target.BLOCK_ENTITY, "item");
	public static final ContainerIO DECORATED_POT = new SingleItemContainerIO(Target.BLOCK_ENTITY, "item");
	
	private static final Map<Item, ContainerIO> CONTAINERS;
	static {
		CONTAINERS = new HashMap<>();
		
		// Main containers
		for (Item shulkerBox : MVRegistry.ITEM) {
			if (shulkerBox instanceof BlockItem block && block.getBlock() instanceof ShulkerBoxBlock)
				CONTAINERS.put(shulkerBox, CHEST);
		}
		CONTAINERS.put(Items.CHEST, CHEST);
		CONTAINERS.put(Items.TRAPPED_CHEST, CHEST);
		CONTAINERS.put(Items.BARREL, CHEST);
		
		// Other containers
		CONTAINERS.put(Items.ARMOR_STAND, ENTITY);
		for (Item spawnEgg : MVRegistry.ITEM) {
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
		CONTAINERS.put(Items.BUNDLE, BUNDLE);
		Version.newSwitch()
				.range("1.20.0", null, () -> {
					CONTAINERS.put(Items.CHISELED_BOOKSHELF, CHISELED_BOOKSHELF);
					CONTAINERS.put(Items.SUSPICIOUS_SAND, SUSPICIOUS_SAND);
					CONTAINERS.put(Items.SUSPICIOUS_GRAVEL, SUSPICIOUS_GRAVEL);
					CONTAINERS.put(Items.DECORATED_POT, DECORATED_POT);
				})
				.range(null, "1.19.4", () -> {})
				.run();
	}
	public static void registerContainer(Item item, ContainerIO container) {
		CONTAINERS.put(item, container);
	}
	
	public static boolean isContainer(ItemStack item) {
		ContainerIO io = CONTAINERS.get(item.getItem());
		if (io == null)
			return false;
		return io.isReadable(item);
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
	
	
	
	public boolean isReadable(ItemStack container) {
		return true;
	}
	public abstract ItemStack[] readItems(ItemStack container);
	public abstract void writeItems(ItemStack container, ItemStack[] contents);
	
}
