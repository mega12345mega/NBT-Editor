package com.luneruniverse.minecraft.mod.nbteditor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;

public class ClientChest {
	
	private static final File CLIENT_CHEST_FOLDER = new File(NBTEditorClient.SETTINGS_FOLDER, "client_chest");
	
	private final int maxPages;
	private final HashMap<Integer, ItemStack[]> pages;
	private volatile boolean loaded;
	
	public ClientChest(int maxPages) {
		this.maxPages = maxPages;
		pages = new HashMap<>();
		loaded = false;
	}
	
	public int getMaxPages() {
		return maxPages;
	}
	public ItemStack[] getPage(int page) {
		checkLoaded();
		return pages.getOrDefault(page, new ItemStack[54]);
	}
	
	public void loadAllAync() {
		Thread loader = new Thread(() -> {
			try {
				loadAllSync();
			} catch (Exception e) {
				NBTEditor.LOGGER.error("Unable to load the client chest!", e);
			}
		}, "NBTEditor/Async/ClientChestLoader");
		loader.setDaemon(true);
		loader.start();
	}
	public void loadAllSync() throws Exception {
		loaded = false;
		for (int i = 0; i < maxPages; i++) {
			try {
				loadSync(i);
			} catch (Exception e) {
				pages.clear();
				throw new Exception("While loading page " + (i + 1) + ":", e);
			}
		}
		loaded = true;
	}
	public void loadSync(int page) throws Exception {
		File file = new File(CLIENT_CHEST_FOLDER, "page" + page + ".nbt");
		if (!file.exists()) {
			pages.remove(page);
			return;
		}
		
		NbtList pageNbt = NbtIo.read(file).getList("items", NbtElement.COMPOUND_TYPE);
		ItemStack[] items = new ItemStack[54];
		boolean empty = true;
		int i = -1;
		for (NbtElement item : pageNbt) {
			items[++i] = ItemStack.fromNbt((NbtCompound) item);
			if (empty && items[i] != null && !items[i].isEmpty())
				empty = false;
		}
		if (empty) {
			pages.remove(page);
			file.delete();
		} else
			pages.put(page, items);
	}
	
	public boolean isLoaded() {
		return loaded;
	}
	private void checkLoaded() {
		if (!loaded)
			throw new IllegalStateException("The client chest isn't loaded yet!");
	}
	
	public void setPage(int page, ItemStack[] items) throws IOException {
		checkLoaded();
		if (!CLIENT_CHEST_FOLDER.exists())
			CLIENT_CHEST_FOLDER.mkdir();
		File file = new File(CLIENT_CHEST_FOLDER, "page" + page + ".nbt");
		
		boolean allAir = true;
		for (ItemStack item : items) {
			if (item != null && !item.isEmpty()) {
				allAir = false;
				break;
			}
		}
		if (allAir) {
			pages.remove(page);
			Files.deleteIfExists(file.toPath());
			return;
		}
		
		pages.put(page, items);
		
		NbtCompound nbt = new NbtCompound();
		NbtList pageNbt = new NbtList();
		for (int i = 0; i < items.length; i++)
			pageNbt.add((items[i] == null ? ItemStack.EMPTY : items[i]).writeNbt(new NbtCompound()));
		nbt.put("items", pageNbt);
		NbtIo.write(nbt, file);
	}
	
	public int[] getNearestItems(int page) {
		checkLoaded();
		
		int prev = -1;
		int next = -1;
		
		for (int i : pages.keySet()) {
			if (i < page && i > prev)
				prev = i;
			if (i > page && (i < next || next == -1))
				next = i;
		}
		
		return new int[] {prev, next};
	}
	
}
