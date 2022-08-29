package com.luneruniverse.minecraft.mod.nbteditor;

import java.io.File;
import java.io.IOException;

import com.luneruniverse.minecraft.mod.nbteditor.async.HeadRefreshThread;
import com.luneruniverse.minecraft.mod.nbteditor.commands.CommandHandler;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ClientChestHandler;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ClientChestScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ItemsHandler;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ItemsScreen;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import tsp.headdb.ported.HeadAPI;

public class NBTEditorClient implements ClientModInitializer {
	
	public static final ScreenHandlerType<ClientChestHandler> CLIENT_CHEST_SCREEN_HANDLER = Registry.register(Registry.SCREEN_HANDLER, new Identifier("nbteditor", "client_chest"), new ScreenHandlerType<>(ClientChestHandler::new));
	public static final ScreenHandlerType<ItemsHandler> ITEMS_SCREEN_HANDLER = Registry.register(Registry.SCREEN_HANDLER, new Identifier("nbteditor", "items"), new ScreenHandlerType<>(ItemsHandler::new));
	
	public static boolean CLIENT_LOADED = false;
	public static final File SETTINGS_FOLDER = new File("nbteditor");
	public static final ClientChest CLIENT_CHEST = new ClientChest(100);
	
	@Override
	public void onInitializeClient() {
		if (!SETTINGS_FOLDER.exists())
			SETTINGS_FOLDER.mkdir();
		
		NbtTypeModifier.modify();
		HandledScreens.register(CLIENT_CHEST_SCREEN_HANDLER, ClientChestScreen::new);
		HandledScreens.register(ITEMS_SCREEN_HANDLER, ItemsScreen::new);
		CommandHandler.registerCommands();
		try {
			HeadAPI.loadFavorites();
		} catch (IOException e) {
			NBTEditor.LOGGER.error("Error while loading HeadDB favorites", e);
		}
		new HeadRefreshThread().start();
		ConfigScreen.loadSettings();
		
		CLIENT_CHEST.loadAllAync();
	}
	
}
