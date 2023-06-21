package com.luneruniverse.minecraft.mod.nbteditor.clientchest;

import java.util.HashMap;

import net.minecraft.item.ItemStack;

public class SmallClientChest extends ClientChest {
	
	private final int maxPages;
	private final HashMap<Integer, ItemStack[]> pages;
	private volatile boolean loaded;
	
	public SmallClientChest(int maxPages) {
		this.maxPages = maxPages;
		pages = new HashMap<>();
		loaded = false;
	}
	
	@Override
	public void loadSync() throws Exception {
		loaded = false;
		Exception toThrow = new Exception("Error loading page(s)");
		for (int i = 0; i < maxPages; i++) {
			try {
				loadSync(i);
			} catch (Exception e) {
				backupCorruptPage(i);
				pages.remove(i);
				toThrow.addSuppressed(new Exception("Page " + (i + 1), e));
			}
		}
		loaded = true;
		if (toThrow.getSuppressed().length > 0)
			throw toThrow;
	}
	
	@Override
	public boolean isLoaded() {
		return loaded;
	}
	
	@Override
	public int getPageCount() {
		return maxPages;
	}
	@Override
	public ItemStack[] getPage(int page) {
		checkLoaded();
		return pages.getOrDefault(page, new ItemStack[54]);
	}
	
	@Override
	protected void cachePage(int page, ItemStack[] items) {
		pages.put(page, items);
	}
	@Override
	protected void cacheEmptyPage(int page) {
		pages.remove(page);
	}
	
	@Override
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
