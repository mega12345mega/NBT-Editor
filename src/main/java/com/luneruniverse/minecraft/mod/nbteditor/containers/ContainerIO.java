package com.luneruniverse.minecraft.mod.nbteditor.containers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalBlock;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalEntity;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalItem;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalItemStack;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalNBT;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.NBTManagers;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVClientNetworking;
import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.TagNames;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.block.Block;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BundleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.math.BlockPos;

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
	public static void registerBlockEntityTagIO(BlockItem blockItem, Function<BlockEntityType<?>, BlockEntityTagContainerIO> io) {
		registerBlockEntityTagIO(blockItem, io.apply(((BlockWithEntity) blockItem.getBlock())
				.createBlockEntity(BlockPos.ORIGIN, blockItem.getBlock().getDefaultState()).getType()));
	}
	public static void registerEntityTagIO(Item item, EntityType<?> entity, EntityTagContainerIO io) {
		registerItemIO(item, io);
		registerEntityIO(entity, io);
	}
	public static void registerEntityTagIO(Item item, EntityType<?> entity, Function<EntityType<?>, EntityTagContainerIO> io) {
		registerEntityTagIO(item, entity, io.apply(entity));
	}
	
	private static final BlockEntityTagContainerIO CHEST_IO = new BlockEntityTagContainerIO(new ConstSizeContainerIO(27));
	private static final BlockEntityTagContainerIO FURNACE_IO = new BlockEntityTagContainerIO(new ConstSizeContainerIO(3));
	private static final BlockEntityTagContainerIO BREWING_STAND_IO = new BlockEntityTagContainerIO(new ConstSizeContainerIO(5));
	private static final BlockEntityTagContainerIO CAMPFIRE_IO = new BlockEntityTagContainerIO(new ConstSizeContainerIO(4));
	private static final BlockEntityTagContainerIO DISPENSER_IO = new BlockEntityTagContainerIO(new ConstSizeContainerIO(9));
	private static final BlockEntityTagContainerIO HOPPER_IO = new BlockEntityTagContainerIO(new ConstSizeContainerIO(5));
	private static final BlockEntityTagContainerIO JUKEBOX_IO = new BlockEntityTagContainerIO(new SpecificItemsContainerIO("RecordItem").withItemSupport(BlockEntityType.JUKEBOX));
	private static final BlockEntityTagContainerIO LECTERN_IO = new BlockEntityTagContainerIO(new SpecificItemsContainerIO("Book").withItemSupport(BlockEntityType.LECTERN));
	private static final Function<EntityType<?>, EntityTagContainerIO> ITEM_FRAME_IO = entity -> new EntityTagContainerIO(new SpecificItemsContainerIO("Item").withItemSupport(entity));
	private static final ItemContainerIO BUNDLE_IO = ItemContainerIO.forNBTIO(new DynamicSizeContainerIO(TagNames.BUNDLE_CONTENTS, 27).withItemSupport((String) null));
	private static final BlockEntityTagContainerIO CHISELED_BOOKSHELF_IO = new ChiseledBookshelfContainerIO();
	private static final BlockEntityTagContainerIO SUSPICIOUS_SAND_IO = new BlockEntityTagContainerIO(new SpecificItemsContainerIO("item").withItemSupport(BlockEntityType.BRUSHABLE_BLOCK));
	private static final BlockEntityTagContainerIO DECORATED_POT_IO = (NBTManagers.COMPONENTS_EXIST ?
			new BlockEntityTagContainerIO(new ConstSizeContainerIO(1), new SpecificItemsContainerIO("item")) :
				new BlockEntityTagContainerIO(new SpecificItemsContainerIO("item").withItemSupport(BlockEntityType.DECORATED_POT)));
	private static final BlockEntityTagContainerIO CRAFTER_IO = new BlockEntityTagContainerIO(new ConstSizeContainerIO(9));
	private static final ItemContainerIO SPAWN_EGG_IO = new SpawnEggContainerIO();
	private static final Function<EntityType<?>, EntityTagContainerIO> ARMOR_HANDS_IO = entity -> new EntityTagContainerIO(new ArmorHandsContainerIO().withItemSupport(entity));
	private static final EntityContainerIO HORSE_IO = EntityContainerIO.forNBTIO(new SpecificItemsContainerIO("SaddleItem", TagNames.ARMOR_ITEM));
	private static final EntityContainerIO BASIC_HORSE_IO = EntityContainerIO.forNBTIO(new SpecificItemsContainerIO("SaddleItem"));
	private static final EntityContainerIO DONKEY_IO = EntityContainerIO.forNBTIO(new ConcatNonItemNBTContainerIO(new SpecificItemsContainerIO("SaddleItem"), new DonkeyChestContainerIO(false)));
	private static final EntityContainerIO LLAMA_IO = EntityContainerIO.forNBTIO(new ConcatNonItemNBTContainerIO(new SpecificItemsContainerIO(TagNames.ARMOR_ITEM), new DonkeyChestContainerIO(true)));
	private static final EntityContainerIO CHEST_BOAT_IO = new EntityTagContainerIO(new ConstSizeContainerIO(27));
	
	public static void loadClass() {}
	
	static {
		// Main containers
		registerBlockEntityTagIO((BlockItem) Items.CHEST, CHEST_IO);
		registerBlockEntityTagIO((BlockItem) Items.TRAPPED_CHEST, CHEST_IO);
		registerBlockEntityTagIO((BlockItem) Items.BARREL, CHEST_IO);
		for (Item item : MVRegistry.ITEM) {
			if (item instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock)
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
		for (Item item : MVRegistry.ITEM) {
			if (item instanceof BundleItem bundle)
				registerItemIO(bundle, BUNDLE_IO);
		}
		Version.newSwitch()
				.range("1.20.0", null, () -> {
					registerBlockEntityTagIO((BlockItem) Items.CHISELED_BOOKSHELF, CHISELED_BOOKSHELF_IO);
					registerBlockEntityTagIO((BlockItem) Items.SUSPICIOUS_SAND, SUSPICIOUS_SAND_IO);
					registerBlockEntityTagIO((BlockItem) Items.SUSPICIOUS_GRAVEL, SUSPICIOUS_SAND_IO);
					registerBlockEntityTagIO((BlockItem) Items.DECORATED_POT, DECORATED_POT_IO);
				})
				.range(null, "1.19.4", () -> {})
				.run();
		Version.newSwitch()
				.range("1.21.0", null, () -> registerBlockEntityTagIO((BlockItem) Items.CRAFTER, CRAFTER_IO))
				.range(null, "1.20.6", () -> {})
				.run();
		
		registerEntityTagIO(Items.ARMOR_STAND, EntityType.ARMOR_STAND, ARMOR_HANDS_IO);
		for (Item item : MVRegistry.ITEM) {
			if (item instanceof SpawnEggItem spawnEgg)
				registerItemIO(spawnEgg, SPAWN_EGG_IO);
		}
		
		registerEntityIO(EntityType.HORSE, HORSE_IO);
		registerEntityIO(EntityType.SKELETON_HORSE, BASIC_HORSE_IO);
		registerEntityIO(EntityType.ZOMBIE_HORSE, BASIC_HORSE_IO);
		Version.newSwitch()
				.range("1.20.0", null, () -> registerEntityIO(EntityType.CAMEL, BASIC_HORSE_IO))
				.range(null, "1.19.4", () -> {})
				.run();
		registerEntityIO(EntityType.DONKEY, DONKEY_IO);
		registerEntityIO(EntityType.MULE, DONKEY_IO);
		registerEntityIO(EntityType.LLAMA, LLAMA_IO);
		registerEntityIO(EntityType.TRADER_LLAMA, LLAMA_IO);
		MVClientNetworking.PlayNetworkStateEvents.Join.EVENT.register(() -> {
			for (EntityType<?> entityType : MVRegistry.ENTITY_TYPE) {
				if (ENTITY_IO.containsKey(entityType))
					continue;
				Entity entity = ServerMVMisc.createEntity(entityType, MainUtil.client.world);
				if (entity instanceof MobEntity)
					registerEntityIO(entityType, ARMOR_HANDS_IO.apply(entityType));
				Version.newSwitch()
						.range("1.19.0", null, () -> {
							if (entity instanceof ChestBoatEntity)
								registerEntityIO(entityType, CHEST_BOAT_IO);
						})
						.range(null, "1.18.2", () -> {})
						.run();
			}
		});
	}
	
	public static int getMaxSize(Item item) {
		ItemContainerIO io = ITEM_IO.get(item);
		return io == null ? 0 : io.getMaxItemSize(null);
	}
	public static int getMaxSize(Block block) {
		BlockContainerIO io = BLOCK_IO.get(block);
		return io == null ? 0 : io.getMaxBlockSize(null);
	}
	public static int getMaxSize(EntityType<?> entity) {
		EntityContainerIO io = ENTITY_IO.get(entity);
		return io == null ? 0 : io.getMaxEntitySize(null);
	}
	
	public static int getMaxSize(ItemStack item) {
		ItemContainerIO io = ITEM_IO.get(item.getItem());
		return io == null ? 0 : io.getMaxItemSize(item);
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
	public static int write(ItemStack container, ItemStack[] contents) {
		return ITEM_IO.get(container.getItem()).writeItem(container, contents);
	}
	
	public static int getMaxSize(LocalNBT nbt) {
		if (nbt instanceof LocalItem item) {
			ItemContainerIO io = ITEM_IO.get(item.getItemType());
			return io == null ? 0 : io.getMaxItemSize(item.getReadableItem());
		}
		if (nbt instanceof LocalBlock block) {
			BlockContainerIO io = BLOCK_IO.get(block.getBlock());
			return io == null ? 0 : io.getMaxBlockSize(block);
		}
		if (nbt instanceof LocalEntity entity) {
			EntityContainerIO io = ENTITY_IO.get(entity.getEntityType());
			return io == null ? 0 : io.getMaxEntitySize(entity);
		}
		return 0;
	}
	public static boolean isContainer(LocalNBT nbt) {
		if (nbt instanceof LocalItem item) {
			ItemContainerIO io = ITEM_IO.get(item.getItemType());
			return io != null && io.isItemReadable(item.getReadableItem());
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
			output = ITEM_IO.get(item.getItemType()).readItem(item.getReadableItem());
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
	public static int write(LocalNBT container, ItemStack[] contents) {
		if (container instanceof LocalItem item)
			return ITEM_IO.get(item.getItemType()).writeItem(item.getEditableItem(), contents);
		if (container instanceof LocalBlock block)
			return BLOCK_IO.get(block.getBlock()).writeBlock(block, contents);
		if (container instanceof LocalEntity entity)
			return ENTITY_IO.get(entity.getEntityType()).writeEntity(entity, contents);
		throw new IllegalArgumentException("Not a container!");
	}
	
	private static void writeRecursively(LocalNBT container, Supplier<ItemStack> subContainers, List<ItemStack> contents, String path) {
		int maxSize = getMaxSize(container);
		
		if (contents.size() <= maxSize) {
			write(container, contents.toArray(ItemStack[]::new));
			return;
		}
		
		ItemStack[] sections = new ItemStack[maxSize];
		
		int sectionSize = maxSize;
		while (contents.size() / sectionSize > maxSize)
			sectionSize *= maxSize;
		for (int i = 0; i < maxSize; i++) {
			if (i * sectionSize >= contents.size())
				break;
			
			ItemStack section = subContainers.get();
			String subPath = (path == null ? i + "" : path + "." + i);
			section.manager$setCustomName(TextInst.of(TextInst.translatable("nbteditor.hdb.section").getString() + ": " + subPath));
			writeRecursively(new LocalItemStack(section), subContainers, contents.subList(i * sectionSize, Math.min(contents.size(), (i + 1) * sectionSize)), subPath);
			sections[i] = section;
		}
		
		write(container, sections);
	}
	public static void writeRecursively(LocalNBT container, Supplier<ItemStack> subContainers, List<ItemStack> contents) {
		writeRecursively(container, subContainers, contents, null);
	}
	public static void writeRecursively(ItemStack container, List<ItemStack> contents) {
		writeRecursively(new LocalItemStack(container), () -> new ItemStack(Items.SHULKER_BOX), contents);
	}
	
}
