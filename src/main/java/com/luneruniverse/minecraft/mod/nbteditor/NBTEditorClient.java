package com.luneruniverse.minecraft.mod.nbteditor;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.luneruniverse.minecraft.mod.nbteditor.addons.NBTEditorAPI;
import com.luneruniverse.minecraft.mod.nbteditor.addons.NBTEditorAddon;
import com.luneruniverse.minecraft.mod.nbteditor.async.HeadRefreshThread;
import com.luneruniverse.minecraft.mod.nbteditor.clientchest.ClientChest;
import com.luneruniverse.minecraft.mod.nbteditor.clientchest.LargeClientChest;
import com.luneruniverse.minecraft.mod.nbteditor.clientchest.SmallClientChest;
import com.luneruniverse.minecraft.mod.nbteditor.commands.CommandHandler;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ClientChestHandler;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ClientChestScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.CursorHistoryHandler;
import com.luneruniverse.minecraft.mod.nbteditor.screens.CursorHistoryScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ItemsHandler;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ItemsScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import tsp.headdb.ported.HeadAPI;

public class NBTEditorClient implements ClientModInitializer {
	
	public static final ScreenHandlerType<ClientChestHandler> CLIENT_CHEST_SCREEN_HANDLER =
			MultiVersionRegistry.register(MultiVersionRegistry.SCREEN_HANDLER,
					new Identifier("nbteditor", "client_chest"),
					new ScreenHandlerType<>(ClientChestHandler::new));
	public static final ScreenHandlerType<ItemsHandler> ITEMS_SCREEN_HANDLER =
			MultiVersionRegistry.register(MultiVersionRegistry.SCREEN_HANDLER,
					new Identifier("nbteditor", "items"),
					new ScreenHandlerType<>(ItemsHandler::new));
	public static final ScreenHandlerType<CursorHistoryHandler> CURSOR_HISTORY_SCREEN_HANDLER =
			MultiVersionRegistry.register(MultiVersionRegistry.SCREEN_HANDLER,
					new Identifier("nbteditor", "cursor_history"),
					new ScreenHandlerType<>(CursorHistoryHandler::new));
	
	public static boolean CLIENT_LOADED = false;
	public static final File SETTINGS_FOLDER = new File("nbteditor");
	public static ClientChest CLIENT_CHEST;
	
	private static final Map<String, NBTEditorAddon> addons = new HashMap<>();
	public static NBTEditorAddon getAddon(String modId) {
		return addons.get(modId);
	}
	public static Map<String, NBTEditorAddon> getAddons() {
		return Collections.unmodifiableMap(addons);
	}
	
	@Override
	public void onInitializeClient() {
		if (!SETTINGS_FOLDER.exists())
			SETTINGS_FOLDER.mkdir();
		
		NbtTypeModifier.loadClass();
		HandledScreens.register(CLIENT_CHEST_SCREEN_HANDLER, ClientChestScreen::new);
		HandledScreens.register(ITEMS_SCREEN_HANDLER, ItemsScreen::new);
		HandledScreens.register(CURSOR_HISTORY_SCREEN_HANDLER, CursorHistoryScreen::new);
		CommandHandler.registerCommands();
		try {
			HeadAPI.loadFavorites();
		} catch (IOException e) {
			NBTEditor.LOGGER.error("Error while loading HeadDB favorites", e);
		}
		new HeadRefreshThread().start();
		ConfigScreen.loadSettings();
		
		CLIENT_CHEST = ConfigScreen.isLargeClientChest() ? new LargeClientChest(5) : new SmallClientChest(100);
		CLIENT_CHEST.loadAync();
		
		NBTEditorAPI.registerInventoryTab(new ItemStack(Items.ENDER_CHEST)
				.setCustomName(TextInst.translatable("itemGroup.nbteditor.client_chest")), ClientChestScreen::show);
		NBTEditorAPI.registerInventoryTab(new ItemStack(Items.BRICKS)
				.setCustomName(TextInst.translatable("itemGroup.nbteditor.creative")),
				() -> MainUtil.client.setScreen(new InventoryScreen(MainUtil.client.player)),
				screen -> screen instanceof ClientChestScreen);
		
		for (EntrypointContainer<NBTEditorAddon> container : FabricLoader.getInstance()
				.getEntrypointContainers("nbteditor", NBTEditorAddon.class)) {
			addons.put(container.getProvider().getMetadata().getId(), container.getEntrypoint());
		}
		addons.forEach((id, addon) -> addon.onInit());
	}
	
}
