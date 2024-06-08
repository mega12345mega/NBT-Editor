package com.luneruniverse.minecraft.mod.nbteditor.containers;

import java.util.HashMap;
import java.util.Map;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalBlock;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalEntity;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalItem;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalNBT;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;

public class ContainerIO {
	
	private static final Map<Item, ItemContainerIO> ITEM_IO = new HashMap<>();
	private static final Map<Block, BlockContainerIO> BLOCK_IO = new HashMap<>();
	private static final Map<EntityType<?>, EntityContainerIO> ENTITY_IO = new HashMap<>();
	
	public static void registerItemIO(Item item, ItemContainerIO io) {
		ITEM_IO.put(item, io);
	}
	public static void registerBlockIO(Block block, BlockContainerIO io) {
		BLOCK_IO.put(block, io);
	}
	public static void registerEntityIO(EntityType<?> entity, EntityContainerIO io) {
		ENTITY_IO.put(entity, io);
	}
	public static void registerBlockEntityTagIO(BlockItem blockItem, BlockEntityTagContainerIO io) {
		registerItemIO(blockItem, io);
		registerBlockIO(blockItem.getBlock(), io);
	}
	public static void registerEntityTagIO(Item item, EntityType<?> entity, EntityTagContainerIO io) {
		registerItemIO(item, io);
		registerEntityIO(entity, io);
	}
	
	private static final BlockEntityTagContainerIO CHEST_IO = new BlockEntityTagContainerIO(new ConstSizeContainerIO(27));
	private static final BlockEntityTagContainerIO FURNACE_IO = new BlockEntityTagContainerIO(new ConstSizeContainerIO(3));
	private static final BlockEntityTagContainerIO BREWING_STAND_IO = new BlockEntityTagContainerIO(new ConstSizeContainerIO(5));
	private static final BlockEntityTagContainerIO CAMPFIRE_IO = new BlockEntityTagContainerIO(new ConstSizeContainerIO(4));
	private static final BlockEntityTagContainerIO DISPENSER_IO = new BlockEntityTagContainerIO(new ConstSizeContainerIO(9));
	private static final BlockEntityTagContainerIO HOPPER_IO = new BlockEntityTagContainerIO(new ConstSizeContainerIO(5));
	private static final BlockEntityTagContainerIO JUKEBOX_IO = new BlockEntityTagContainerIO(new SpecificItemsContainerIO("RecordItem"));
	private static final BlockEntityTagContainerIO LECTERN_IO = new BlockEntityTagContainerIO(new SpecificItemsContainerIO("Book"));
	private static final EntityTagContainerIO ITEM_FRAME_IO = new EntityTagContainerIO(new SpecificItemsContainerIO("Item"));
	private static final ItemContainerIO BUNDLE_IO = ItemContainerIO.forNBTIO(new DynamicSizeContainerIO(27));
	private static final BlockEntityTagContainerIO CHISELED_BOOKSHELF_IO = new ChiseledBookshelfContainerIO();
	private static final BlockEntityTagContainerIO SUSPICIOUS_SAND_IO = new BlockEntityTagContainerIO(new SpecificItemsContainerIO("item"));
	private static final BlockEntityTagContainerIO DECORATED_POT_IO = new BlockEntityTagContainerIO(new SpecificItemsContainerIO("item"));
	private static final ItemContainerIO SPAWN_EGG_IO = new SpawnEggContainerIO();
	private static final EntityTagContainerIO ARMOR_HANDS_IO = new EntityTagContainerIO(new ArmorHandsContainerIO());
	private static final EntityTagContainerIO HORSE_IO = new EntityTagContainerIO(new SpecificItemsContainerIO("SaddleItem", "ArmorItem"));
	
	public static void loadClass() {}
	
	static {
		// Main containers
		registerBlockEntityTagIO((BlockItem) Items.CHEST, CHEST_IO);
		registerBlockEntityTagIO((BlockItem) Items.TRAPPED_CHEST, CHEST_IO);
		registerBlockEntityTagIO((BlockItem) Items.BARREL, CHEST_IO);
		for (Item item : MVRegistry.ITEM) {
			if (item instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock block)
				registerBlockEntityTagIO(blockItem, CHEST_IO);
		}
		
		// Other containers
		registerBlockEntityTagIO((BlockItem) Items.FURNACE, FURNACE_IO);
		registerBlockEntityTagIO((BlockItem) Items.BLAST_FURNACE, FURNACE_IO);
		registerBlockEntityTagIO((BlockItem) Items.SMOKER, FURNACE_IO);
		
		registerBlockEntityTagIO((BlockItem) Items.BREWING_STAND, BREWING_STAND_IO);
		registerBlockEntityTagIO((BlockItem) Items.CAMPFIRE, CAMPFIRE_IO);
		registerBlockEntityTagIO((BlockItem) Items.SOUL_CAMPFIRE, CAMPFIRE_IO);
		registerBlockEntityTagIO((BlockItem) Items.DISPENSER, DISPENSER_IO);
		registerBlockEntityTagIO((BlockItem) Items.DROPPER, DISPENSER_IO);
		registerBlockEntityTagIO((BlockItem) Items.HOPPER, HOPPER_IO);
		registerBlockEntityTagIO((BlockItem) Items.JUKEBOX, JUKEBOX_IO);
		registerBlockEntityTagIO((BlockItem) Items.LECTERN, LECTERN_IO);
		registerEntityTagIO(Items.ITEM_FRAME, EntityType.ITEM_FRAME, ITEM_FRAME_IO);
		registerEntityTagIO(Items.GLOW_ITEM_FRAME, EntityType.GLOW_ITEM_FRAME, ITEM_FRAME_IO);
		registerItemIO(Items.BUNDLE, BUNDLE_IO);
		Version.newSwitch()
				.range("1.20.0", null, () -> {
					registerBlockEntityTagIO((BlockItem) Items.CHISELED_BOOKSHELF, CHISELED_BOOKSHELF_IO);
					registerBlockEntityTagIO((BlockItem) Items.SUSPICIOUS_SAND, SUSPICIOUS_SAND_IO);
					registerBlockEntityTagIO((BlockItem) Items.SUSPICIOUS_GRAVEL, SUSPICIOUS_SAND_IO);
					registerBlockEntityTagIO((BlockItem) Items.DECORATED_POT, DECORATED_POT_IO);
				})
				.range(null, "1.19.4", () -> {})
				.run();
		
		registerItemIO(Items.ARMOR_STAND, ARMOR_HANDS_IO);
		for (Item item : MVRegistry.ITEM) {
			if (item instanceof SpawnEggItem spawnEgg)
				registerItemIO(spawnEgg, SPAWN_EGG_IO);
		}
		
		registerEntityIO(EntityType.HORSE, HORSE_IO);
		ClientPlayConnectionEvents.JOIN.register((network, sender, client) -> {
			for (EntityType<?> entity : MVRegistry.ENTITY_TYPE) {
				if (ENTITY_IO.containsKey(entity))
					continue;
				if (entity.create(client.world) instanceof MobEntity)
					registerEntityIO(entity, ARMOR_HANDS_IO);
			}
		});
	}
	
	public static boolean isContainer(ItemStack item) {
		ItemContainerIO io = ITEM_IO.get(item.getItem());
		return io != null && io.isItemReadable(item);
	}
	public static ItemStack[] read(ItemStack container) {
		ItemStack[] output = ITEM_IO.get(container.getItem()).readItem(container);
		for (int i = 0; i < output.length; i++) {
			if (output[i] == null)
				output[i] = ItemStack.EMPTY;
		}
		return output;
	}
	public static void write(ItemStack container, ItemStack[] contents) {
		ITEM_IO.get(container.getItem()).writeItem(container, contents);
	}
	
	public static boolean isContainer(LocalNBT nbt) {
		if (nbt instanceof LocalItem item) {
			ItemContainerIO io = ITEM_IO.get(item.getItem().getItem());
			return io != null && io.isItemReadable(item.getItem());
		}
		if (nbt instanceof LocalBlock block) {
			BlockContainerIO io = BLOCK_IO.get(block.getBlock());
			return io != null && io.isBlockReadable(block);
		}
		if (nbt instanceof LocalEntity entity) {
			EntityContainerIO io = ENTITY_IO.get(entity.getEntityType());
			return io != null && io.isEntityReadable(entity);
		}
		return false;
	}
	public static ItemStack[] read(LocalNBT container) {
		ItemStack[] output = null;
		if (container instanceof LocalItem item)
			output = ITEM_IO.get(item.getItem().getItem()).readItem(item.getItem());
		if (container instanceof LocalBlock block)
			output = BLOCK_IO.get(block.getBlock()).readBlock(block);
		if (container instanceof LocalEntity entity)
			output = ENTITY_IO.get(entity.getEntityType()).readEntity(entity);
		if (output == null)
			throw new IllegalArgumentException("Not a container!");
		for (int i = 0; i < output.length; i++) {
			if (output[i] == null)
				output[i] = ItemStack.EMPTY;
		}
		return output;
	}
	public static void write(LocalNBT container, ItemStack[] contents) {
		if (container instanceof LocalItem item) {
			ITEM_IO.get(item.getItem().getItem()).writeItem(item.getItem(), contents);
			return;
		}
		if (container instanceof LocalBlock block) {
			BLOCK_IO.get(block.getBlock()).writeBlock(block, contents);
			return;
		}
		if (container instanceof LocalEntity entity) {
			ENTITY_IO.get(entity.getEntityType()).writeEntity(entity, contents);
			return;
		}
		throw new IllegalArgumentException("Not a container!");
	}
	
}
