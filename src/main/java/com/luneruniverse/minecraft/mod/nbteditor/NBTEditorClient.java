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
import com.luneruniverse.minecraft.mod.nbteditor.misc.NbtTypeModifier;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.ClientChestScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.Enchants;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import tsp.headdb.ported.HeadAPI;

public class NBTEditorClient implements ClientModInitializer {
	
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
		Enchants.checkCap();
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
