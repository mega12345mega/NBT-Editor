package com.luneruniverse.minecraft.mod.nbteditor.clientchest;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;

import net.minecraft.item.ItemStack;

public class LargeClientChest extends ClientChest {
	
	private final int importantPages;
	private final Cache<Integer, Optional<ItemStack[]>> pages;
	private volatile boolean loaded;
	
	public LargeClientChest(int importantPages) {
		this.importantPages = importantPages;
		pages = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build();
	}
	
	@Override
	public void loadSync() throws Exception {
		loaded = false;
		for (int i = 0; i < importantPages; i++) {
			try {
				loadSync(i);
			} catch (Exception e) {
				pages.invalidateAll();
				throw new Exception("While loading page " + (i + 1) + ":", e);
			}
		}
		loaded = true;
	}
	
	@Override
	public boolean isLoaded() {
		return loaded;
	}
	
	@Override
	public int getPageCount() {
		return Integer.MAX_VALUE;
	}
	@Override
	public ItemStack[] getPage(int page) {
		try {
			return pages.get(page, () -> Optional.ofNullable(loadSync(page))).orElseGet(() -> new ItemStack[54]);
		} catch (ExecutionException e) {
			NBTEditor.LOGGER.error("Error while loading a large client chest page", e);
			return new ItemStack[54];
		}
	}
	
	@Override
	protected void cachePage(int page, ItemStack[] items) {
		pages.put(page, Optional.of(items));
	}
	@Override
	protected void cacheEmptyPage(int page) {
		pages.put(page, Optional.empty());
	}
	
	@Override
	public int[] getNearestItems(int page) {
		int prev = -1;
		int next = -1;
		
		// Assumes all page files have items (which is true unless they were edited externally)
		for (File file : CLIENT_CHEST_FOLDER.listFiles()) {
			if (!file.getName().matches("page[0-9]+\\.nbt"))
				continue;
			int i = Integer.parseInt(file.getName().substring("page".length(), file.getName().indexOf('.')));
			if (i < page && i > prev)
				prev = i;
			if (i > page && (i < next || next == -1))
				next = i;
		}
		
		return new int[] {prev, next};
	}
	
}
