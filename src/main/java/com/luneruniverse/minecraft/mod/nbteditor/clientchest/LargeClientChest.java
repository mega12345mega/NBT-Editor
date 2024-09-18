package com.luneruniverse.minecraft.mod.nbteditor.clientchest;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;

public class LargeClientChest extends ClientChest {
	
	private final int importantPages;
	private final Cache<Integer, ClientChestPage> pages;
	private volatile boolean loaded;
	
	public LargeClientChest(int importantPages) {
		this.importantPages = importantPages;
		pages = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build();
	}
	
	@Override
	public void loadSync() throws Exception {
		loaded = false;
		Exception toThrow = new Exception("Error loading page(s)");
		for (int i = 0; i < importantPages; i++) {
			try {
				loadSync(i);
			} catch (Exception e) {
				backupCorruptPage(i);
				pages.invalidate(i);
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
		pages.asMap().values().forEach(ClientChestPage::tryLoadDynamicItems);
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
	public ClientChestPage getPage(int page) {
		try {
			return pages.get(page, () -> loadSync(page));
		} catch (ExecutionException | UncheckedExecutionException e) {
			backupCorruptPage(page);
			pages.invalidate(page);
			NBTEditor.LOGGER.error("Error loading large client chest page " + (page + 1), e);
			return new ClientChestPage();
		}
	}
	
	@Override
	protected boolean isPageCached(int page) {
		return pages.getIfPresent(page) != null;
	}
	@Override
	protected void cachePage(int page, ClientChestPage items) {
		pages.put(page, items);
	}
	@Override
	protected void cacheEmptyPage(int page) {
		pages.put(page, new ClientChestPage());
	}
	@Override
	protected void discardPageCache(int page) throws Exception {
		pages.invalidate(page);
	}
	
	@Override
	public int[] getNearestItems(int page) {
		if (!CLIENT_CHEST_FOLDER.exists())
			return new int[] {-1, -1};
		
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
