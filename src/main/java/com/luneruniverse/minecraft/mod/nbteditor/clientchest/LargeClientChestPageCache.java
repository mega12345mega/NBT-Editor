package com.luneruniverse.minecraft.mod.nbteditor.clientchest;

import java.io.File;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.minecraft.item.ItemStack;

public class LargeClientChestPageCache implements ClientChestPageCache {
	
	private final int importantPages;
	private final Cache<Integer, ClientChestPage> pages;
	
	public LargeClientChestPageCache(int importantPages) {
		this.importantPages = importantPages;
		this.pages = CacheBuilder.newBuilder().softValues().expireAfterAccess(30, TimeUnit.MINUTES).build();
	}
	
	@Override
	public int getPageCount() {
		return Integer.MAX_VALUE;
	}
	@Override
	public int getDefaultLoadedPagesCount() {
		return importantPages;
	}
	
	@Override
	public ClientChestPage getCachedPage(int page) {
		ClientChestPage output = pages.getIfPresent(page);
		if (output == null) {
			output = ClientChestPage.unloaded();
			pages.put(page, output);
		}
		return output;
	}
	@Override
	public void cachePage(int page, ClientChestPage pageData) {
		pages.put(page, pageData);
	}
	@Override
	public void cacheEmptyPage(int page) {
		pages.put(page, new ClientChestPage());
	}
	@Override
	public void discardPageCache(int page) {
		pages.invalidate(page);
	}
	
	@Override
	public int[] getNearestItems(int page) {
		if (!ClientChest.CLIENT_CHEST_FOLDER.exists())
			return new int[] {-1, -1};
		
		int prev = -1;
		int next = -1;
		
		// Assumes all page files have items (which is true unless they were edited externally)
		for (File file : ClientChest.CLIENT_CHEST_FOLDER.listFiles()) {
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
	
	@Override
	public void transferTo(ClientChestPageCache cache) {
		int maxPages = cache.getPageCount();
		
		pages.asMap().forEach((page, pageData) -> {
			if (page >= maxPages)
				return;
			
			boolean empty = pageData.isInThisVersion() && pageData.dynamicItems().getSlots().isEmpty();
			if (empty) {
				for (ItemStack item : pageData.items()) {
					if (item != null && !item.isEmpty()) {
						empty = false;
						break;
					}
				}
			}
			
			if (empty)
				cache.cacheEmptyPage(page);
			else
				cache.cachePage(page, pageData);
		});
	}
	
}
