package com.luneruniverse.minecraft.mod.nbteditor.clientchest;

import java.util.HashMap;

public class SmallClientChest extends ClientChest {
	
	private final int maxPages;
	private final HashMap<Integer, ClientChestPage> pages;
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
	protected void tryLoadDynamicItemsSync() {
		checkLoaded();
		loaded = false;
		pages.values().forEach(ClientChestPage::tryLoadDynamicItems);
		loaded = true;
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
	public ClientChestPage getPage(int page) {
		checkLoaded();
		ClientChestPage output = pages.get(page);
		if (output == null)
			output = new ClientChestPage();
		return output;
	}
	
	@Override
	protected boolean isPageCached(int page) {
		return true;
	}
	@Override
	protected void cachePage(int page, ClientChestPage items) {
		pages.put(page, items);
	}
	@Override
	protected void cacheEmptyPage(int page) {
		pages.remove(page);
	}
	@Override
	protected void discardPageCache(int page) throws Exception {
		try {
			loadSync(page);
		} catch (Exception e) {
			backupCorruptPage(page);
			pages.remove(page);
			throw e;
		}
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
