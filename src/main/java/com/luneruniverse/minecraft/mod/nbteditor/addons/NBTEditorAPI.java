package com.luneruniverse.minecraft.mod.nbteditor.addons;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.commands.factories.FactoryCommand;
import com.luneruniverse.minecraft.mod.nbteditor.commands.get.GetCommand;
import com.luneruniverse.minecraft.mod.nbteditor.commands.get.GetPresetCommand;
import com.luneruniverse.minecraft.mod.nbteditor.containers.BlockContainerIO;
import com.luneruniverse.minecraft.mod.nbteditor.containers.BlockEntityTagContainerIO;
import com.luneruniverse.minecraft.mod.nbteditor.containers.ContainerIO;
import com.luneruniverse.minecraft.mod.nbteditor.containers.EntityContainerIO;
import com.luneruniverse.minecraft.mod.nbteditor.containers.EntityTagContainerIO;
import com.luneruniverse.minecraft.mod.nbteditor.containers.ItemContainerIO;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.NBTReference;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.NBTReferenceFilter;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigCategory;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigPath;
import com.luneruniverse.minecraft.mod.nbteditor.screens.factories.LocalFactoryScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.nbtfolder.NBTFolder;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.CreativeTabWidget;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.luneruniverse.minecraft.mod.nbteditor.util.NbtFormatter;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.datafixers.DSL.TypeReference;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;

/**
 * The main API<br>
 * Use to register your features
 */
public class NBTEditorAPI {
	
	public static NBTEditorAddon getAddon(String modId) {
		return NBTEditorClient.getAddon(modId);
	}
	public static Map<String, NBTEditorAddon> getAddons() {
		return NBTEditorClient.getAddons();
	}
	
	/**
	 * Register an advanced factory<br>
	 * Allows for inputting arguments<br>
	 * Keep in mind NBT Editor uses a modified version of Fabric's command API to support multiple Minecraft versions<br>
	 * Refer to the {@link com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands} package<br>
	 * Example usage:<br>
	 * <pre>
	 * <code>
	 * NBTEditorAPI.registerAdvancedFactory("myfactory", builder -> {
	 * 	builder.executes(context -> {
	 * 		ItemReference ref = MainUtil.getHeldItem();
	 * 		ItemStack item = ref.getItem();
	 * 		// Manipulate item
	 * 		ref.saveItem(item, () -> MainUtil.client.player.sendMessage(TextInst.literal("Myfactory complete!")));
	 * 		return Command.SINGLE_SUCCESS;
	 * 	});
	 * });
	 * </code>
	 * </pre>
	 * @param name The name of the factory (used in the factory command)
	 * @param extremeAlias The extreme alias
	 * @param onRegister A consumer for the {@code /factory <name>} argument builder
	 * @see #registerFactory(String, Consumer)
	 * @see #registerFactory(String, Text, Consumer)
	 */
	public static void registerAdvancedFactory(String name, String extremeAlias, Consumer<LiteralArgumentBuilder<FabricClientCommandSource>> onRegister) {
		FactoryCommand.INSTANCE.getChildren().add(new ClientCommand() {
			@Override
			public String getName() {
				return name;
			}
			@Override
			public String getExtremeAlias() {
				return extremeAlias;
			}
			@Override
			public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder, String path) {
				onRegister.accept(builder);
			}
		});
	}
	
	/**
	 * Register a normal factory
	 * @param name The name of the factory (used in the factory command)
	 * @param extremeAlias The extreme alias
	 * @param factory A consumer for the {@link NBTReference} the factory is called on
	 * @see #registerFactory(String, Text, Consumer)
	 * @see #registerAdvancedFactory(String, Consumer)
	 */
	public static void registerFactory(String name, String extremeAlias, Consumer<NBTReference<?>> factory) {
		registerAdvancedFactory(name, extremeAlias, builder -> builder.executes(context -> {
			NBTReference.getReference(NBTReferenceFilter.ANY, false, factory);
			return Command.SINGLE_SUCCESS;
		}));
	}
	
	/**
	 * Register a normal factory, adding it to the factory gui<br>
	 * <strong>Warning:</strong> If this factory opens its own GUI, make sure call {@link NBTReference#showParent()}
	 * (recommended) or {@link NBTReference#escapeParent()} when the GUI is closed. This ensures that, if the user
	 * has an item on their cursor, it doesn't get lost.
	 * @param name The name of the factory (used in the factory command)
	 * @param extremeAlias The extreme alias
	 * @param buttonMsg The text to display in the factory gui
	 * @param supported If the factory supports the particular reference (item vs. entity, etc.)
	 * @param unsupportedMsg The message to show in chat if the command is called on an unsupported reference
	 * @param factory A consumer for the {@link NBTReference} the factory is called on
	 * @see #registerFactory(String, Consumer)
	 * @see #registerAdvancedFactory(String, Consumer)
	 */
	public static void registerFactory(String name, String extremeAlias, Text buttonMsg,
			Predicate<NBTReference<?>> supported, Text unsupportedMsg, Consumer<NBTReference<?>> factory) {
		registerFactory(name, extremeAlias, ref -> {
			if (supported.test(ref))
				factory.accept(ref);
			else if (MainUtil.client.player != null)
				MainUtil.client.player.sendMessage(unsupportedMsg, false);
		});
		LocalFactoryScreen.BASIC_FACTORIES.add(new LocalFactoryScreen.LocalFactoryReference(buttonMsg, supported, factory));
	}
	
	/**
	 * Register a get command<br>
	 * Keep in mind NBT Editor uses a modified version of Fabric's command API to support multiple Minecraft versions<br>
	 * Refer to the {@link com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands} package<br>
	 * @param name The name of the item that will be received (used in the get command)
	 * @param extremeAlias The extreme alias
	 * @param onRegister A consumer for the {@code /get <name>} argument builder
	 * @see #registerAdvancedFactory(String, Consumer) An example of using onRegister
	 * @see MainUtil#getWithMessage(ItemStack)
	 */
	public static void registerGetCommand(String name, String extremeAlias, Consumer<LiteralArgumentBuilder<FabricClientCommandSource>> onRegister) {
		GetCommand.INSTANCE.getChildren().add(new ClientCommand() {
			@Override
			public String getName() {
				return name;
			}
			@Override
			public String getExtremeAlias() {
				return extremeAlias;
			}
			@Override
			public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder, String path) {
				onRegister.accept(builder);
			}
		});
	}
	
	/**
	 * Add a preset item (/get preset {@literal <name>})
	 * @param name The name of the item
	 * @param item The item to add
	 * @see #registerPresetItem(String)
	 */
	public static void registerPresetItem(String name, Supplier<ItemStack> item) {
		GetPresetCommand.registerPresetItem(name, item);
	}
	
	/**
	 * Add a preset item<br>
	 * The item is automatically loaded from {@code assets/nbteditor/presetitems/<name>.nbt}<br>
	 * The item's NBT is in the format <code>{id: "minecraft:diamond", Count: 64b, tag: {}}</code><br>
	 * Automatically export an item with {@code /nbt export file}
	 * @param name The name of the item
	 * @param reloadable If the item should be re-read every time, allowing for changing it via resource packs
	 * @return The loaded item, or null if the item couldn't be loaded
	 * @see #registerPresetItem(String, ItemStack)
	 */
	public static Supplier<ItemStack> registerPresetItem(String name, boolean reloadable) {
		Supplier<ItemStack> item = GetPresetCommand.registerPresetItem(name);
		Supplier<ItemStack> reloadHandledItem = item;
		if (!reloadable) {
			reloadHandledItem = new Supplier<ItemStack>() {
				private ItemStack value;
				@Override
				public ItemStack get() {
					if (value == null)
						value = item.get();
					return value;
				}
			};
			GetPresetCommand.registerPresetItem(name, reloadHandledItem);
		}
		return reloadHandledItem;
	}
	
	/**
	 * Register a container<br>
	 * This is used with the {@code /open} command to edit special containers (like item frames)
	 * @param item The item that this container applies to
	 * @param container The container reader and writer
	 * @see #registerBlockEntityTagContainer(BlockItem, BlockEntityTagContainerIO)
	 * @see #registerBlockEntityTagContainer(BlockItem, Function)
	 * @see #registerEntityTagContainer(Item, EntityType, EntityTagContainerIO)
	 * @see #registerEntityTagContainer(Item, EntityType, Function)
	 */
	public static void registerItemContainer(Item item, ItemContainerIO container) {
		ContainerIO.registerItemIO(item, container);
	}
	/**
	 * Register a container<br>
	 * This is used with the {@code /open} command to edit special containers (like item frames)
	 * @param block The block that this container applies to
	 * @param container The container reader and writer
	 * @see #registerBlockEntityTagContainer(BlockItem, BlockEntityTagContainerIO)
	 * @see #registerBlockEntityTagContainer(BlockItem, Function)
	 */
	public static void registerBlockContainer(Block block, BlockContainerIO container) {
		ContainerIO.registerBlockIO(block, container);
	}
	/**
	 * Register a container<br>
	 * This is used with the {@code /open} command to edit special containers (like item frames)
	 * @param entity The entity that this container applies to, including spawn eggs of this type
	 * @param container The container reader and writer
	 * @see #registerEntityTagContainer(Item, EntityType, EntityTagContainerIO)
	 * @see #registerEntityTagContainer(Item, EntityType, Function)
	 */
	public static void registerEntityContainer(EntityType<?> entity, EntityContainerIO container) {
		ContainerIO.registerEntityIO(entity, container);
	}
	/**
	 * Register a container<br>
	 * This is used with the {@code /open} command to edit special containers (like item frames)
	 * @param blockItem The item and block that this container applies to
	 * @param container The container reader and writer
	 * @see #registerItemContainer(Item, ItemContainerIO)
	 * @see #registerBlockContainer(Block, BlockContainerIO)
	 * @see #registerBlockEntityTagContainer(BlockItem, Function)
	 */
	public static void registerBlockEntityTagContainer(BlockItem blockItem, BlockEntityTagContainerIO container) {
		ContainerIO.registerBlockEntityTagIO(blockItem, container);
	}
	/**
	 * Register a container<br>
	 * This is used with the {@code /open} command to edit special containers (like item frames)
	 * @param blockItem The item and block that this container applies to
	 * @param container The container reader and writer
	 * @see #registerItemContainer(Item, ItemContainerIO)
	 * @see #registerBlockContainer(Block, BlockContainerIO)
	 * @see #registerBlockEntityTagContainer(BlockItem, BlockEntityTagContainerIO)
	 */
	public static void registerBlockEntityTagContainer(BlockItem blockItem, Function<BlockEntityType<?>, BlockEntityTagContainerIO> container) {
		ContainerIO.registerBlockEntityTagIO(blockItem, container);
	}
	/**
	 * Register a container<br>
	 * This is used with the {@code /open} command to edit special containers (like item frames)<br>
	 * DO NOT pass in the spawn egg to <code>item</code>; use {@link #registerEntityContainer(EntityType, EntityContainerIO)}
	 * @param item The item that this container applies to
	 * @param entity The entity that this container applies to, including spawn eggs of this type
	 * @param container The container reader and writer
	 * @see #registerItemContainer(Item, ItemContainerIO)
	 * @see #registerEntityContainer(EntityType, EntityContainerIO)
	 * @see #registerEntityTagContainer(Item, EntityType, Function)
	 */
	public static void registerEntityTagContainer(Item item, EntityType<?> entity, EntityTagContainerIO container) {
		ContainerIO.registerEntityTagIO(item, entity, container);
	}
	/**
	 * Register a container<br>
	 * This is used with the {@code /open} command to edit special containers (like item frames)<br>
	 * DO NOT pass in the spawn egg to <code>item</code>; use {@link #registerEntityContainer(EntityType, EntityContainerIO)}
	 * @param item The item that this container applies to
	 * @param entity The entity that this container applies to, including spawn eggs of this type
	 * @param container The container reader and writer
	 * @see #registerItemContainer(Item, ItemContainerIO)
	 * @see #registerEntityContainer(EntityType, EntityContainerIO)
	 * @see #registerEntityTagContainer(Item, EntityType, EntityTagContainerIO)
	 */
	public static void registerEntityTagContainer(Item item, EntityType<?> entity, Function<EntityType<?>, EntityTagContainerIO> container) {
		ContainerIO.registerEntityTagIO(item, entity, container);
	}
	
	/**
	 * Register a way to open usually unopenable NBT types in the editor<br>
	 * This technique is used to allow opening parsable strings<br>
	 * This could be used to open a number as a list of bits for example<br>
	 * This is an advanced, somewhat obscure feature of the API
	 * @param clazz The NBT class to make openable (can be a superclass)
	 * @param folder The constructor for the folder manager
	 */
	public static void registerNBTFolderType(Class<? extends NbtElement> clazz, NBTFolder.Constructor<?> folder) {
		NBTFolder.TYPES.put(clazz, folder);
	}
	
	/**
	 * Add an option to the config screen
	 * @param item A consumer of the global config category, called every time the config screen is opened
	 * @see #registerConfigItem(String, Supplier)
	 * @see ConfigCategory#setConfigurable(String, ConfigPath)
	 */
	public static void registerConfigItem(Consumer<ConfigCategory> item) {
		ConfigScreen.ADDED_OPTIONS.add(item);
	}
	
	/**
	 * Add an option to the config screen
	 * @param name The internal name of the option - this must be unique
	 * @param item The option to add, called every time the config screen is opened
	 * @see #registerConfigItem(Consumer)
	 */
	public static void registerConfigItem(String name, Supplier<ConfigPath> item) {
		registerConfigItem(config -> config.setConfigurable(name, item.get()));
	}
	
	/**
	 * Create a tab on the screen, located in the bottom left
	 * @param item The item to display on the tab
	 * @param onClick Called when the tab is clicked
	 * @param whenToShow Set which screens the tab should appear on
	 * @see #registerInventoryTab(ItemStack, Runnable)
	 */
	public static void registerInventoryTab(ItemStack item, Runnable onClick, Predicate<Screen> whenToShow) {
		CreativeTabWidget.TABS.add(new CreativeTabWidget.CreativeTabData(item, onClick, whenToShow));
	}
	
	/**
	 * Create a tab on the screen, located in the bottom left<br>
	 * Shows the tab when in the creative inventory
	 * @param item The item to display on the tab
	 * @param onClick Called when the tab is clicked
	 * @see #registerInventoryTab(ItemStack, Runnable, Predicate)
	 */
	public static void registerInventoryTab(ItemStack item, Runnable onClick) {
		registerInventoryTab(item, onClick, screen -> screen instanceof CreativeInventoryScreen);
	}
	
	/**
	 * Set the formatter used in the editor screen
	 * @param formatter The formatter
	 */
	public static void setNBTFormatter(NbtFormatter.Impl formatter) {
		NbtFormatter.FORMATTER = formatter;
	}
	
	/**
	 * Updates old NBT structures into the current Minecraft version
	 * @see #updateNBTDynamic(TypeReference, NbtCompound)
	 * @see #updateNBTDynamic(TypeReference, NbtCompound, int)
	 * @see #updateNBTDynamic(TypeReference, NbtElement, NbtElement, int)
	 */
	public static <T extends NbtElement> T updateNBT(TypeReference typeRef, T nbt, int oldVersion) {
		return MainUtil.update(typeRef, nbt, oldVersion);
	}
	/**
	 * Updates old NBT structures into the current Minecraft version<br>
	 * If dataVersionTag is not null and a number, this updates from that - otherwise, this updates from defaultOldVersion
	 * @see #updateNBT(TypeReference, NbtElement, int)
	 * @see #updateNBTDynamic(TypeReference, NbtCompound)
	 * @see #updateNBTDynamic(TypeReference, NbtCompound, int)
	 */
	public static <T extends NbtElement> T updateNBTDynamic(TypeReference typeRef, T nbt, NbtElement dataVersionTag, int defaultOldVersion) {
		return MainUtil.updateDynamic(typeRef, nbt, dataVersionTag, defaultOldVersion);
	}
	/**
	 * Updates old NBT structures into the current Minecraft version<br>
	 * If a DataVersion tag exists, this updates from that - otherwise, this updates from defaultOldVersion
	 * @see #updateNBT(TypeReference, NbtElement, int)
	 * @see #updateNBTDynamic(TypeReference, NbtCompound)
	 * @see #updateNBTDynamic(TypeReference, NbtElement, NbtElement, int)
	 */
	public static NbtCompound updateNBTDynamic(TypeReference typeRef, NbtCompound nbt, int defaultOldVersion) {
		return MainUtil.updateDynamic(typeRef, nbt, defaultOldVersion);
	}
	/**
	 * Updates old NBT structures into the current Minecraft version<br>
	 * If a DataVersion tag exists, this updates from that - otherwise, nbt is returned
	 * @see #updateNBT(TypeReference, NbtElement, int)
	 * @see #updateNBTDynamic(TypeReference, NbtCompound, int)
	 * @see #updateNBTDynamic(TypeReference, NbtElement, NbtElement, int)
	 */
	public static NbtCompound updateNBTDynamic(TypeReference typeRef, NbtCompound nbt) {
		return MainUtil.updateDynamic(typeRef, nbt);
	}
	
}
