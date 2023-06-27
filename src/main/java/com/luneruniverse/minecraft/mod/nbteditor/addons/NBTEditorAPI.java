package com.luneruniverse.minecraft.mod.nbteditor.addons;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.commands.factories.FactoryCommand;
import com.luneruniverse.minecraft.mod.nbteditor.commands.get.GetCommand;
import com.luneruniverse.minecraft.mod.nbteditor.commands.get.GetPresetCommand;
import com.luneruniverse.minecraft.mod.nbteditor.containers.ContainerIO;
import com.luneruniverse.minecraft.mod.nbteditor.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.misc.NbtTypeModifier;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.CreativeTab;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigCategory;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigPath;
import com.luneruniverse.minecraft.mod.nbteditor.screens.factories.ItemFactoryScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.nbtmenugenerators.MenuGenerator;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.luneruniverse.minecraft.mod.nbteditor.util.NbtFormatter;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtType;
import net.minecraft.nbt.NbtTypes;
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
	 * Register an advanced item factory<br>
	 * Allows for inputting arguments<br>
	 * Keep in mind NBT Editor uses a modified version of Fabric's command API to support multiple Minecraft versions<br>
	 * Refer to the {@link com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands} package<br>
	 * Example usage:<br>
	 * <pre>
	 * <code>
	 * NBTEditorAPI.registerAdvancedItemFactory("myfactory", builder -> {
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
	 * @param name The name of the item factory (used in the itemfactory command)
	 * @param onRegister A consumer for the {@code /itemfactory <name>} argument builder
	 * @see #registerItemFactory(String, Consumer)
	 * @see #registerItemFactory(String, Text, Consumer)
	 */
	public static void registerAdvancedItemFactory(String name, Consumer<LiteralArgumentBuilder<FabricClientCommandSource>> onRegister) {
		FactoryCommand.INSTANCE.getChildren().add(new ClientCommand() {
			@Override
			public String getName() {
				return name;
			}
			@Override
			public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
				onRegister.accept(builder);
			}
		});
	}
	
	/**
	 * Register a normal item factory
	 * @param name The name of the item factory (used in the itemfactory command)
	 * @param factory A consumer for the {@link ItemReference} the factory is called on
	 * @see #registerItemFactory(String, Text, Consumer)
	 * @see #registerAdvancedItemFactory(String, Consumer)
	 */
	public static void registerItemFactory(String name, Consumer<ItemReference> factory) {
		registerAdvancedItemFactory(name, builder -> builder.executes(context -> {
			factory.accept(ItemReference.getHeldItem());
			return Command.SINGLE_SUCCESS;
		}));
	}
	
	/**
	 * Register a normal item factory, adding it to the factory gui
	 * @param name The name of the item factory (used in the itemfactory command)
	 * @param buttonMsg The text to display in the itemfactory gui
	 * @param supported If the button should display for the current item
	 * @param factory A consumer for the {@link ItemReference} the factory is called on
	 * @see #registerItemFactory(String, Consumer)
	 * @see #registerAdvancedItemFactory(String, Consumer)
	 */
	public static void registerItemFactory(String name, Text buttonMsg, Predicate<ItemReference> supported, Consumer<ItemReference> factory) {
		registerItemFactory(name, factory);
		ItemFactoryScreen.BASIC_FACTORIES.add(new ItemFactoryScreen.ItemFactoryReference(buttonMsg, supported, factory));
	}
	
	/**
	 * Register a get command<br>
	 * Keep in mind NBT Editor uses a modified version of Fabric's command API to support multiple Minecraft versions<br>
	 * Refer to the {@link com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands} package<br>
	 * @param name The name of the item that will be received (used in the get command)
	 * @param onRegister A consumer for the {@code /get <name>} argument builder
	 * @see #registerAdvancedItemFactory(String, Consumer) An example of using onRegister
	 * @see MainUtil#getWithMessage(ItemStack)
	 */
	public static void registerGetCommand(String name, Consumer<LiteralArgumentBuilder<FabricClientCommandSource>> onRegister) {
		GetCommand.INSTANCE.getChildren().add(new ClientCommand() {
			@Override
			public String getName() {
				return name;
			}
			@Override
			public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
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
	 */
	public static void registerContainer(Item item, ContainerIO container) {
		ContainerIO.registerContainer(item, container);
	}
	
	/**
	 * Register a way to open usually unopenable NBT types in the editor<br>
	 * This technique is used to allow opening parsable strings<br>
	 * This could be used to open a number as a list of bits for example<br>
	 * You must properly handle immutable types to prevent issues<br>
	 * This is an advanced, somewhat obscure feature of the API
	 * @param type The NBT type to open
	 * @param generator The generator for the editor's menu
	 * @see #makeMutable(byte)
	 */
	public static void registerNBTMenuGenerator(byte type, MenuGenerator generator) {
		MenuGenerator.TYPES.put(type, generator);
	}
	
	/**
	 * The NBT type is set to mutable<br>
	 * You must create a mixin for the nbt element, overriding copy and getNbtType
	 * @param type The NBT type to modify
	 * @return The new NBT type that should be returned from getNbtType
	 * @see #registerNBTMenuGenerator(byte, MenuGenerator)
	 */
	public static NbtType<?> makeMutable(byte type) {
		return NbtTypeModifier.makeMutable(NbtTypes.byId(type));
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
		CreativeTab.TABS.add(new CreativeTab.CreativeTabData(item, onClick, whenToShow));
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
	
}
