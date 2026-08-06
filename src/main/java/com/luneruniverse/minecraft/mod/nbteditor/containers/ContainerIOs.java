package com.luneruniverse.minecraft.mod.nbteditor.containers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalBlock;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalEntity;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalItem;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalItemStack;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalNBT;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVClientNetworking;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.registry.MVRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.ItemTagReferences;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BoatItem;
import net.minecraft.item.BundleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public class ContainerIOs {
	
	private static final Map<Item, ContainerIO<ItemStack>> ITEM_IO = new HashMap<>();
	private static final Map<Block, ContainerIO<LocalBlock>> BLOCK_IO = new HashMap<>();
	private static final Map<EntityType<?>, ContainerIO<LocalEntity>> ENTITY_IO = new HashMap<>();
	
	public static void registerItemIO(Item item, ContainerIO<ItemStack> io) {
		ITEM_IO.put(item, io);
	}
	public static void registerBlockIO(Block block, ContainerIO<LocalBlock> io) {
		BLOCK_IO.put(block, io);
	}
	public static void registerEntityIO(EntityType<?> entity, ContainerIO<LocalEntity> io) {
		ENTITY_IO.put(entity, io);
	}
	
	public static void registerItemBlockIO(BlockItem blockItem, ItemBlockContainerIO io) {
		registerItemIO(blockItem, io.item());
		registerBlockIO(blockItem.getBlock(), io.block());
	}
	
	public static void registerItemEntityIO(Item item, EntityType<?> entity, ItemEntityContainerIO io) {
		registerItemIO(item, io.item());
		registerEntityIO(entity, io.entity());
	}
	public static void registerItemEntityIO(Item item, EntityType<?> entity, Function<EntityType<?>, ItemEntityContainerIO> io) {
		registerItemEntityIO(item, entity, io.apply(entity));
	}
	
	private static final ItemBlockContainerIO CHEST_IO = ItemBlockContainerIO.forSlotKeyItems(27);
	private static final ItemBlockContainerIO FURNACE_IO = ItemBlockContainerIO.forSlotKeyItems(3);
	private static final ItemBlockContainerIO BREWING_STAND_IO = ItemBlockContainerIO.forSlotKeyItems(5)
			.withTextures(ContainerIO.POTION_TEXTURE, ContainerIO.POTION_TEXTURE, ContainerIO.POTION_TEXTURE,
					null, ContainerIO.BREWING_FUEL_TEXTURE);
	private static final ItemBlockContainerIO CAMPFIRE_IO = ItemBlockContainerIO.forSlotKeyItems(4);
	private static final ItemBlockContainerIO DISPENSER_IO = ItemBlockContainerIO.forSlotKeyItems(9);
	private static final ItemBlockContainerIO HOPPER_IO = ItemBlockContainerIO.forSlotKeyItems(5);
	private static final ItemBlockContainerIO JUKEBOX_IO = ItemBlockContainerIO.forKeys(BlockEntityType.JUKEBOX, "RecordItem");
	private static final ItemBlockContainerIO LECTERN_IO = BlockStateUpdatingContainerIO.forItemBlock(
			ItemBlockContainerIO.forKeys(BlockEntityType.LECTERN, "Book"), "has_book");
	private static final Function<EntityType<?>, ItemEntityContainerIO> ITEM_FRAME_IO =
			entityId -> ItemEntityContainerIO.forKeys(entityId, "Item");
	private static final ContainerIO<ItemStack> BUNDLE_IO = Version.<ContainerIO<ItemStack>>newSwitch()
			.range("1.20.5", null, () -> new BundleContentsComponentContainerIO(27))
			.range(null, "1.20.4", () -> ContainerIO.forItemStack(new OrderNbtListContainerIO(27).forNbtCompoundItems()))
			.get();
	private static final ItemBlockContainerIO CHISELED_BOOKSHELF_IO = Version.<ItemBlockContainerIO>newSwitch()
			.range("1.20.0", null, () -> BlockStateUpdatingContainerIO.forItemBlock(
					ItemBlockContainerIO.forSlotKeyItems(6), "slot_0_occupied", "slot_1_occupied", "slot_2_occupied",
					"slot_3_occupied", "slot_4_occupied", "slot_5_occupied"))
			.range(null, "1.19.4", () -> null)
			.get();
	private static final ItemBlockContainerIO SUSPICIOUS_SAND_IO = Version.<ItemBlockContainerIO>newSwitch()
			.range("1.20.0", null, () -> ItemBlockContainerIO.forKeys(BlockEntityType.BRUSHABLE_BLOCK, "item"))
			.range(null, "1.19.4", () -> null)
			.get();
	private static final ItemBlockContainerIO DECORATED_POT_IO = Version.<ItemBlockContainerIO>newSwitch()
			.range("1.20.5", null, () -> new ItemBlockContainerIO(
					new ContainerComponentContainerIO(1), ContainerIO.forLocalNBT(new KeysContainerIO(false, "item"))))
			.range("1.20.3", "1.20.4", () -> ItemBlockContainerIO.forKeys(BlockEntityType.DECORATED_POT, "item"))
			.range(null, "1.20.2", () -> null)
			.get();
	private static final ItemBlockContainerIO CRAFTER_IO = Version.<ItemBlockContainerIO>newSwitch()
			.range("1.21.0", null, () -> ItemBlockContainerIO.forSlotKeyItems(9))
			.range(null, "1.20.6", () -> null)
			.get();
	private static final ContainerIO<ItemStack> SPAWN_EGG_IO = new DelegateContainerIO<>(
			(item, entity) -> get(entity),
			item -> new LocalEntity(MVMisc.getEntityType(item), ItemTagReferences.ENTITY_DATA.get(item)),
			(item, entity) -> ItemTagReferences.ENTITY_DATA.set(item, MainUtil.fillId(entity.getNBT(), entity.getId().toString())));
	private static final Function<EntityType<?>, ItemEntityContainerIO> EQUIPMENT_IO =
			entityId -> ItemEntityContainerIO.forEntityTagIO(Version.<ContainerIO<NbtCompound>>newSwitch()
					.range("1.20.5", null, () -> new EquipmentContainerIO(false).forNbtCompoundEquipment())
					.range(null, "1.20.4", () -> new ArmorHandsContainerIO())
					.get(),
					entityId);
	private static final ContainerIO<LocalEntity> HORSE_IO = ContainerIO.forLocalNBT(
			Version.<ContainerIO<NbtCompound>>newSwitch()
					.range("1.21.5", null, () -> new EquipmentContainerIO(false).forNbtCompoundEquipment())
					.range("1.20.5", "1.21.4", () -> new ConcatContainerIO<>(
							new ArmorHandsContainerIO(), new KeysContainerIO(false, "SaddleItem", "body_armor_item")))
					.range(null, "1.20.4", () -> new ConcatContainerIO<>(
							new ArmorHandsContainerIO(), new KeysContainerIO(false, "SaddleItem", "ArmorItem")))
					.get());
	private static final ContainerIO<LocalEntity> BASIC_HORSE_IO = ContainerIO.forLocalNBT(
			Version.<ContainerIO<NbtCompound>>newSwitch()
					.range("1.21.5", null, () -> new EquipmentContainerIO(false).forNbtCompoundEquipment())
					.range(null, "1.21.4", () -> new ConcatContainerIO<>(
							new ArmorHandsContainerIO(), new KeysContainerIO(false, "SaddleItem")))
					.get());
	private static final ContainerIO<LocalEntity> DONKEY_IO = ContainerIO.forLocalNBT(
			Version.<ContainerIO<NbtCompound>>newSwitch()
					.range("1.21.5", null, () -> new ConcatContainerIO<>(
							new EquipmentContainerIO(false).forNbtCompoundEquipment(), new DonkeyChestContainerIO(false)))
					.range(null, "1.21.4", () -> new ConcatContainerIO<>(
							new ArmorHandsContainerIO(), new KeysContainerIO(false, "SaddleItem"), new DonkeyChestContainerIO(false)))
					.get());
	private static final ContainerIO<LocalEntity> LLAMA_IO = ContainerIO.forLocalNBT(
			Version.<ContainerIO<NbtCompound>>newSwitch()
					.range("1.21.5", null, () -> new ConcatContainerIO<>(
							new EquipmentContainerIO(true).forNbtCompoundEquipment(), new DonkeyChestContainerIO(true)))
					.range("1.20.5", "1.21.4", () -> new ConcatContainerIO<>(
							new ArmorHandsContainerIO(), new KeysContainerIO(false, "body_armor_item"), new DonkeyChestContainerIO(true)))
					.range(null, "1.20.4", () -> new ConcatContainerIO<>(
							new ArmorHandsContainerIO(), new KeysContainerIO(false, "DecorItem"), new DonkeyChestContainerIO(true)))
					.get());
	private static final ContainerIO<LocalEntity> VILLAGER_IO = new ConcatContainerIO<>(
			EQUIPMENT_IO.apply(EntityType.VILLAGER).entity(),
			ContainerIO.forLocalNBT(new OrderNbtListContainerIO(8).forNbtCompound("Inventory")));
	private static final ItemEntityContainerIO CHEST_MINECART_IO = ItemEntityContainerIO.forEntityTagIO(
			new SlotKeyNbtListContainerIO(27).forNbtCompoundItems(), EntityType.CHEST_MINECART);
	private static final ItemEntityContainerIO HOPPER_MINECART_IO = ItemEntityContainerIO.forEntityTagIO(
			new SlotKeyNbtListContainerIO(5).forNbtCompoundItems(), EntityType.FURNACE_MINECART);
	private static final Function<EntityType<?>, ItemEntityContainerIO> CHEST_BOAT_IO =
			entityType -> Version.<ItemEntityContainerIO>newSwitch()
					.range("1.19.0", null, () -> ItemEntityContainerIO.forEntityTagIO(
							new SlotKeyNbtListContainerIO(27).forNbtCompoundItems(), entityType))
					.range(null, "1.18.2", () -> null)
					.get();
	private static final ContainerIO<LocalEntity> ALLAY_IO = Version.<ContainerIO<LocalEntity>>newSwitch()
			.range("1.19.0", null, () -> new ConcatContainerIO<>(
					EQUIPMENT_IO.apply(EntityType.ALLAY).entity(),
					ContainerIO.forLocalNBT(new OrderNbtListContainerIO(1).forNbtCompound("Inventory"))))
			.range(null, "1.18.2", () -> null)
			.get();
	
	public static void loadClass() {}
	
	static {
		registerItemBlockIO((BlockItem) Items.CHEST, CHEST_IO);
		registerItemBlockIO((BlockItem) Items.TRAPPED_CHEST, CHEST_IO);
		registerItemBlockIO((BlockItem) Items.BARREL, CHEST_IO);
		for (Item item : MVRegistry.ITEM) {
			if (item instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock)
				registerItemBlockIO(blockItem, CHEST_IO);
		}
		
		registerItemBlockIO((BlockItem) Items.FURNACE, FURNACE_IO);
		registerItemBlockIO((BlockItem) Items.BLAST_FURNACE, FURNACE_IO);
		registerItemBlockIO((BlockItem) Items.SMOKER, FURNACE_IO);
		
		registerItemBlockIO((BlockItem) Items.BREWING_STAND, BREWING_STAND_IO);
		registerItemBlockIO((BlockItem) Items.CAMPFIRE, CAMPFIRE_IO);
		registerItemBlockIO((BlockItem) Items.SOUL_CAMPFIRE, CAMPFIRE_IO);
		registerItemBlockIO((BlockItem) Items.DISPENSER, DISPENSER_IO);
		registerItemBlockIO((BlockItem) Items.DROPPER, DISPENSER_IO);
		registerItemBlockIO((BlockItem) Items.HOPPER, HOPPER_IO);
		registerItemBlockIO((BlockItem) Items.JUKEBOX, JUKEBOX_IO);
		registerItemBlockIO((BlockItem) Items.LECTERN, LECTERN_IO);
		registerItemEntityIO(Items.ITEM_FRAME, EntityType.ITEM_FRAME, ITEM_FRAME_IO);
		registerItemEntityIO(Items.GLOW_ITEM_FRAME, EntityType.GLOW_ITEM_FRAME, ITEM_FRAME_IO);
		for (Item item : MVRegistry.ITEM) {
			if (item instanceof BundleItem bundle)
				registerItemIO(bundle, BUNDLE_IO);
		}
		Version.newSwitch()
				.range("1.20.0", null, () -> {
					registerItemBlockIO((BlockItem) Items.CHISELED_BOOKSHELF, CHISELED_BOOKSHELF_IO);
					registerItemBlockIO((BlockItem) Items.SUSPICIOUS_SAND, SUSPICIOUS_SAND_IO);
					registerItemBlockIO((BlockItem) Items.SUSPICIOUS_GRAVEL, SUSPICIOUS_SAND_IO);
				})
				.range(null, "1.19.4", () -> {})
				.run();
		Version.newSwitch()
				.range("1.20.3", null, () -> registerItemBlockIO((BlockItem) Items.DECORATED_POT, DECORATED_POT_IO))
				.range(null, "1.20.2", () -> {})
				.run();
		Version.newSwitch()
				.range("1.21.0", null, () -> registerItemBlockIO((BlockItem) Items.CRAFTER, CRAFTER_IO))
				.range(null, "1.20.6", () -> {})
				.run();
		
		registerItemEntityIO(Items.ARMOR_STAND, EntityType.ARMOR_STAND, EQUIPMENT_IO);
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
		registerEntityIO(EntityType.VILLAGER, VILLAGER_IO);
		Version.newSwitch()
				.range("1.20.3", null, () -> {
					registerItemEntityIO(Items.CHEST_MINECART, EntityType.CHEST_MINECART, CHEST_MINECART_IO);
					registerItemEntityIO(Items.HOPPER_MINECART, EntityType.HOPPER_MINECART, HOPPER_MINECART_IO);
				})
				.range(null, "1.20.2", () -> {
					registerEntityIO(EntityType.CHEST_MINECART, CHEST_MINECART_IO.entity());
					registerEntityIO(EntityType.HOPPER_MINECART, HOPPER_MINECART_IO.entity());
				})
				.run();
		Map<EntityType<?>, BoatItem> boatItems = new HashMap<>();
		Version.newSwitch()
				.range("1.21.2", null, () -> {
					for (Item item : MVRegistry.ITEM) {
						if (item instanceof BoatItem boat)
							boatItems.put(boat.boatEntityType, boat);
					}
				})
				.range("1.20.3", "1.21.1", () -> {
					EntityType<?> chestBoat = Reflection.getField(EntityType.class, "field_38096", "Lnet/minecraft/class_1299;").get(null);
					ContainerIO<ItemStack> io = CHEST_BOAT_IO.apply(chestBoat).item();
					registerItemIO(Items.OAK_CHEST_BOAT, io);
					registerItemIO(Items.SPRUCE_CHEST_BOAT, io);
					registerItemIO(Items.BIRCH_CHEST_BOAT, io);
					registerItemIO(Items.JUNGLE_CHEST_BOAT, io);
					registerItemIO(Items.ACACIA_CHEST_BOAT, io);
					registerItemIO(Items.CHERRY_CHEST_BOAT, io);
					registerItemIO(Items.DARK_OAK_CHEST_BOAT, io);
					registerItemIO(Items.MANGROVE_CHEST_BOAT, io);
					registerItemIO(Items.BAMBOO_CHEST_RAFT, io);
				})
				.range(null, "1.20.2", () -> {})
				.run();
		Version.newSwitch()
				.range("1.19.0", null, () -> registerEntityIO(EntityType.ALLAY, ALLAY_IO))
				.range(null, "1.18.2", () -> {})
				.run();
		MVClientNetworking.PlayNetworkStateEvents.Join.EVENT.register(() -> {
			for (EntityType<?> entityType : MVRegistry.ENTITY_TYPE) {
				if (ENTITY_IO.containsKey(entityType))
					continue;
				Entity entity = ServerMVMisc.createEntity(entityType, MainUtil.client.world);
				if (entity instanceof MobEntity)
					registerEntityIO(entityType, EQUIPMENT_IO.apply(entityType).entity());
				Version.newSwitch()
						.range("1.19.0", null, () -> {
							if (entity instanceof ChestBoatEntity) {
								registerEntityIO(entityType, CHEST_BOAT_IO.apply(entityType).entity());
								BoatItem item = boatItems.get(entityType);
								if (item != null)
									registerItemIO(item, CHEST_BOAT_IO.apply(entityType).item());
							}
						})
						.range(null, "1.18.2", () -> {})
						.run();
			}
		});
	}
	
	public static boolean isSupported(ItemStack item) {
		ContainerIO<ItemStack> io = ITEM_IO.get(item.getItem());
		return io != null && io.isSupported(item);
	}
	public static boolean isSupported(LocalItem localItem) {
		ContainerIO<ItemStack> io = ITEM_IO.get(localItem.getItemType());
		return io != null && io.isSupported(localItem.getReadableItem());
	}
	public static boolean isSupported(LocalBlock localBlock) {
		ContainerIO<LocalBlock> io = BLOCK_IO.get(localBlock.getBlock());
		return io != null && io.isSupported(localBlock);
	}
	public static boolean isSupported(LocalEntity localEntity) {
		ContainerIO<LocalEntity> io = ENTITY_IO.get(localEntity.getEntityType());
		return io != null && io.isSupported(localEntity);
	}
	public static boolean isSupported(LocalNBT localNBT) {
		if (localNBT instanceof LocalItem localItem)
			return isSupported(localItem);
		if (localNBT instanceof LocalBlock localBlock)
			return isSupported(localBlock);
		if (localNBT instanceof LocalEntity localEntity)
			return isSupported(localEntity);
		return false;
	}
	
	public static ContainerIO<ItemStack> get(ItemStack container) {
		return ITEM_IO.get(container.getItem());
	}
	public static ContainerIO<LocalItem> get(LocalItem container) {
		ContainerIO<ItemStack> io = ITEM_IO.get(container.getItemType());
		return new DelegateContainerIO<>((localItem, item) -> io, LocalItem::getReadableItem, (localItem, item) -> {}) {
			@Override
			public int write(LocalItem container, ItemStack[] contents) {
				return io.write(container.getEditableItem(), contents);
			}
		};
	}
	public static ContainerIO<LocalBlock> get(LocalBlock container) {
		return BLOCK_IO.get(container.getBlock());
	}
	public static ContainerIO<LocalEntity> get(LocalEntity container) {
		return ENTITY_IO.get(container.getEntityType());
	}
	@SuppressWarnings("unchecked")
	public static <L extends LocalNBT, R> R exec(L container, BiFunction<ContainerIO<? super L>, L, R> method) {
		if (container instanceof LocalItem localItem)
			return method.apply((ContainerIO<? super L>) get(localItem), container);
		if (container instanceof LocalBlock localBlock)
			return method.apply((ContainerIO<? super L>) get(localBlock), container);
		if (container instanceof LocalEntity localEntity)
			return method.apply((ContainerIO<? super L>) get(localEntity), container);
		throw new IllegalArgumentException("Not a container!");
	}
	public static <L extends LocalNBT, R> R exec(L container, Function<ContainerIO<? super L>, R> method) {
		return exec(container, (io, container2) -> method.apply(io));
	}
	
	public static int getMaxSlots(LocalNBT container) {
		return exec(container, ContainerIO::getMaxSlots);
	}
	
	public static Identifier[] getTextures(LocalNBT container) {
		return exec(container, ContainerIO::getTextures);
	}
	
	public static ItemStack[] read(LocalNBT container) {
		return exec(container, ContainerIO::read);
	}
	
	public static int write(LocalNBT container, ItemStack[] contents) {
		return exec(container, io -> io.write(container, contents));
	}
	
	public static int getNumWritten(LocalNBT container, ItemStack[] contents) {
		return exec(container, io -> io.getNumWritten(container, contents));
	}
	
	public static int getWrittenSlotIndex(LocalNBT container, ItemStack[] contents, int slot) {
		return exec(container, io -> io.getWrittenSlotIndex(container, contents, slot));
	}
	
	private static void writeRecursively(LocalNBT container, Supplier<ItemStack> subContainers, List<ItemStack> contents, String path) {
		int maxSlots = getMaxSlots(container);
		
		if (contents.size() <= maxSlots) {
			write(container, contents.toArray(ItemStack[]::new));
			return;
		}
		
		ItemStack[] sections = new ItemStack[maxSlots];
		
		int sectionSize = maxSlots;
		while (contents.size() / sectionSize > maxSlots)
			sectionSize *= maxSlots;
		for (int i = 0; i < maxSlots; i++) {
			if (i * sectionSize >= contents.size())
				break;
			
			ItemStack section = subContainers.get();
			String subPath = (path == null ? i + "" : path + "." + i);
			section.nbte$setCustomName(
					TextInst.of(TextInst.translatable("nbteditor.hdb.section").getString() + ": " + subPath));
			writeRecursively(new LocalItemStack(section), subContainers,
					contents.subList(i * sectionSize, Math.min(contents.size(), (i + 1) * sectionSize)), subPath);
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
