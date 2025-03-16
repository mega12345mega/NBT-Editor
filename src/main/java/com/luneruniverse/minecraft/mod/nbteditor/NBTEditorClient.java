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
import com.luneruniverse.minecraft.mod.nbteditor.clientchest.ClientChestHelper;
import com.luneruniverse.minecraft.mod.nbteditor.clientchest.LargeClientChestPageCache;
import com.luneruniverse.minecraft.mod.nbteditor.clientchest.PageLoadLevel;
import com.luneruniverse.minecraft.mod.nbteditor.clientchest.SmallClientChestPageCache;
import com.luneruniverse.minecraft.mod.nbteditor.commands.CommandHandler;
import com.luneruniverse.minecraft.mod.nbteditor.containers.ContainerIO;
import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.misc.NbtTypeModifier;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVEnchantments;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVClientNetworking;
import com.luneruniverse.minecraft.mod.nbteditor.packets.OpenEnderChestC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.ClientChestScreen;
import com.luneruniverse.minecraft.mod.nbteditor.server.NBTEditorServer;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import tsp.headdb.ported.HeadAPI;

public class NBTEditorClient implements ClientModInitializer {
	
	static {
		MC_1_17_Link.MixinLink.ENCHANT_GLINT_FIX = MixinLink.ENCHANT_GLINT_FIX;
		MC_1_17_Link.MixinLink.USE_DIRECT_ITEM_GLINT_CONSUMER = Version.<Boolean>newSwitch()
				.range("1.21.2", null, false)
				.range(null, "1.21.1", true)
				.get();
		MC_1_17_Link.ConfigScreen.isEnchantGlintFix_impl = ConfigScreen::isEnchantGlintFix;
	}
	
	public static final File SETTINGS_FOLDER = new File("nbteditor");
	public static ClientChest CLIENT_CHEST;
	public static NBTEditorServerConn SERVER_CONN;
	
	private static final Map<String, NBTEditorAddon> addons = new HashMap<>();
	public static NBTEditorAddon getAddon(String modId) {
		return addons.get(modId);
	}
	public static Map<String, NBTEditorAddon> getAddons() {
		return Collections.unmodifiableMap(addons);
	}
	
	@Override
	public void onInitializeClient() {
		NBTEditorServer.IS_DEDICATED = false;
		
		if (!SETTINGS_FOLDER.exists())
			SETTINGS_FOLDER.mkdir();
		
		MVMisc.onRegistriesLoad(this::onRegistriesLoad);
	}
	
	private void onRegistriesLoad() {
		NbtTypeModifier.loadClass();
		CommandHandler.registerCommands();
		try {
			HeadAPI.loadFavorites();
		} catch (IOException e) {
			NBTEditor.LOGGER.error("Error while loading HeadDB favorites", e);
		}
		ContainerIO.loadClass();
		new HeadRefreshThread().start();
		ConfigScreen.loadSettings();
		
		CLIENT_CHEST = new ClientChest(ConfigScreen.isLargeClientChest() ? new LargeClientChestPageCache(5) : new SmallClientChestPageCache(100));
		ClientChestHelper.loadDefaultPages(PageLoadLevel.NORMAL_ITEMS);
		MVClientNetworking.PlayNetworkStateEvents.Start.EVENT.register(networkHandler -> ClientChestHelper.loadDefaultPages(PageLoadLevel.DYNAMIC_ITEMS));
		MVClientNetworking.PlayNetworkStateEvents.Stop.EVENT.register(() -> ClientChestHelper.unloadAllPages(PageLoadLevel.NORMAL_ITEMS));
		
		ItemStack clientChestIcon = new ItemStack(Items.ENDER_CHEST)
				.manager$setCustomName(TextInst.translatable("itemGroup.nbteditor.client_chest"));
		MVEnchantments.addEnchantment(clientChestIcon, MVEnchantments.LOYALTY, 1);
		MixinLink.ENCHANT_GLINT_FIX.add(clientChestIcon);
		NBTEditorAPI.registerInventoryTab(clientChestIcon, ClientChestScreen::show,
				screen -> screen instanceof CreativeInventoryScreen || (screen instanceof InventoryScreen && SERVER_CONN.isEditingExpanded()));
		NBTEditorAPI.registerInventoryTab(new ItemStack(Items.CHEST)
				.manager$setCustomName(TextInst.translatable("itemGroup.nbteditor.inventory")),
				() -> {
					MainUtil.setRootCursorStack(MainUtil.client.player.playerScreenHandler, MainUtil.client.player.currentScreenHandler.getCursorStack());
					MainUtil.client.player.currentScreenHandler = MainUtil.client.player.playerScreenHandler;
					MainUtil.client.setScreen(new InventoryScreen(MainUtil.client.player));
				},
				screen -> screen instanceof ClientChestScreen);
		NBTEditorAPI.registerInventoryTab(new ItemStack(Items.ENDER_CHEST), () -> {
					MainUtil.setInventoryCursorStack(MainUtil.client.player.currentScreenHandler.getCursorStack());
					MainUtil.client.player.closeHandledScreen();
					MVClientNetworking.send(new OpenEnderChestC2SPacket());
				}, screen -> (screen instanceof CreativeInventoryScreen || screen instanceof InventoryScreen || screen instanceof ClientChestScreen)
						&& SERVER_CONN.isEditingExpanded());
		
		SERVER_CONN = new NBTEditorServerConn();
		
		for (EntrypointContainer<NBTEditorAddon> container : FabricLoader.getInstance()
				.getEntrypointContainers("nbteditor", NBTEditorAddon.class)) {
			addons.put(container.getProvider().getMetadata().getId(), container.getEntrypoint());
		}
		addons.forEach((id, addon) -> addon.onInit());
	}
	
}
