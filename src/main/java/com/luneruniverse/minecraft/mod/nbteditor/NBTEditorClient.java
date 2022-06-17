package com.luneruniverse.minecraft.mod.nbteditor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.luneruniverse.minecraft.mod.nbteditor.commands.CommandHandler;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ClientChestHandler;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ClientChestScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ItemsHandler;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ItemsScreen;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import tsp.headdb.ported.HeadAPI;
import tsp.headdb.ported.HeadRefreshThread;

public class NBTEditorClient implements ClientModInitializer {
	
	public static final ScreenHandlerType<ClientChestHandler> CLIENT_CHEST_SCREEN_HANDLER = Registry.register(Registry.SCREEN_HANDLER, new Identifier("nbteditor", "client_chest"), new ScreenHandlerType<>(ClientChestHandler::new));
	public static final ScreenHandlerType<ItemsHandler> ITEMS_SCREEN_HANDLER = Registry.register(Registry.SCREEN_HANDLER, new Identifier("nbteditor", "items"), new ScreenHandlerType<>(ItemsHandler::new));
	
	
	public static final File SETTINGS_FOLDER = new File("nbteditor");
	
	
	private static final File CLIENT_CHEST_FOLDER = new File(SETTINGS_FOLDER, "client_chest");
	private static final Map<Integer, ItemStack[]> CLIENT_CHEST = new HashMap<>();
	public static ItemStack[] getClientChestPage(int page) {
		return CLIENT_CHEST.getOrDefault(page, new ItemStack[54]);
	}
	public static void setClientChestPage(int page, ItemStack[] items) throws IOException {
		CLIENT_CHEST.remove(page);
		
		if (items != null) {
			for (ItemStack item : items) {
				if (item != null && !item.isEmpty()) {
					CLIENT_CHEST.put(page, items);
					break;
				}
			}
		}
		
		// Save to file
		File file = new File(CLIENT_CHEST_FOLDER, "page" + page + ".nbt");
		items = CLIENT_CHEST.get(page);
		if (items != null) {
			NbtCompound nbt = new NbtCompound();
			NbtList pageNbt = new NbtList();
			for (int i = 0; i < items.length; i++)
				pageNbt.add((items[i] == null ? ItemStack.EMPTY : items[i]).writeNbt(new NbtCompound()));
			nbt.put("items", pageNbt);
			NbtIo.write(nbt, file);
		} else
			file.delete();
	}
	public static int[] getClientChestJumpPages(int page) {
		int prev = -1;
		int next = -1;
		
		for (int i : CLIENT_CHEST.keySet()) {
			if (i < page && i > prev)
				prev = i;
			if (i > page && (i < next || next == -1))
				next = i;
		}
		
		return new int[] {prev, next};
	}
	public static void loadClientChestPage(int page) throws IOException {
		File file = new File(CLIENT_CHEST_FOLDER, "page" + page + ".nbt");
		if (!file.exists()) {
			CLIENT_CHEST.remove(page);
			return;
		}
		
		NbtList pageNbt = NbtIo.read(file).getList("items", NbtType.COMPOUND);
		ItemStack[] items = new ItemStack[54];
		boolean empty = true;
		int i = -1;
		for (NbtElement item : pageNbt) {
			items[++i] = ItemStack.fromNbt((NbtCompound) item);
			if (empty && items[i] != null && !items[i].isEmpty())
				empty = false;
		}
		if (empty) {
			CLIENT_CHEST.remove(page);
			file.delete();
		} else
			CLIENT_CHEST.put(page, items);
	}
	public static void loadClientChest() throws IOException {
		for (int page = 0; page < 100; page++)
			loadClientChestPage(page);
	}
	
	
	@Override
	public void onInitializeClient() {
		if (!SETTINGS_FOLDER.exists())
			SETTINGS_FOLDER.mkdir();
		if (!CLIENT_CHEST_FOLDER.exists())
			CLIENT_CHEST_FOLDER.mkdir();
		
		NbtTypeModifier.modify();
		HandledScreens.register(CLIENT_CHEST_SCREEN_HANDLER, ClientChestScreen::new);
		HandledScreens.register(ITEMS_SCREEN_HANDLER, ItemsScreen::new);
		CommandHandler.registerCommands();
		try {
			HeadAPI.loadFavorites();
		} catch (IOException e) {
			e.printStackTrace();
		}
		new HeadRefreshThread().start();
		ConfigScreen.loadSettings();
		
		try {
			loadClientChest();
		} catch (ClassCastException | IOException e) {
			e.printStackTrace();
		}
	}
	
}
