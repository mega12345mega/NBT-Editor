package com.luneruniverse.minecraft.mod.nbteditor.clientchest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;

public abstract class ClientChest {
	
	protected static final File CLIENT_CHEST_FOLDER = new File(NBTEditorClient.SETTINGS_FOLDER, "client_chest");
	
	public void loadAync() {
		Thread loader = new Thread(() -> {
			try {
				loadSync();
			} catch (Exception e) {
				NBTEditor.LOGGER.error("Unable to load the client chest!", e);
			}
		}, "NBTEditor/Async/ClientChestLoader");
		loader.setDaemon(true);
		loader.start();
	}
	public abstract void loadSync() throws Exception;
	public ItemStack[] loadSync(int page) throws Exception {
		File file = new File(CLIENT_CHEST_FOLDER, "page" + page + ".nbt");
		if (!file.exists()) {
			cacheEmptyPage(page);
			return null;
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
			cacheEmptyPage(page);
			file.delete();
			return null;
		} else {
			cachePage(page, items);
			return items;
		}
	}
	
	public abstract boolean isLoaded();
	protected void checkLoaded() {
		if (!isLoaded())
			throw new IllegalStateException("The client chest isn't loaded yet!");
	}
	
	public abstract int getPageCount();
	public abstract ItemStack[] getPage(int page);
	
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
			cacheEmptyPage(page);
			Files.deleteIfExists(file.toPath());
			return;
		}
		
		cachePage(page, items);
		
		NbtCompound nbt = new NbtCompound();
		NbtList pageNbt = new NbtList();
		for (int i = 0; i < items.length; i++)
			pageNbt.add((items[i] == null ? ItemStack.EMPTY : items[i]).writeNbt(new NbtCompound()));
		nbt.put("items", pageNbt);
		NbtIo.write(nbt, file);
	}
	
	protected abstract void cachePage(int page, ItemStack[] items);
	protected abstract void cacheEmptyPage(int page);
	
	public abstract int[] getNearestItems(int page);
	
}
