package com.luneruniverse.minecraft.mod.nbteditor.clientchest;

import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

public class SmallClientChestPageCache implements ClientChestPageCache {
	
	private final int maxPages;
	private final HashMap<Integer, ClientChestPage> pages;
	private final SortedSet<Integer> nonEmptyPages;
	
	public SmallClientChestPageCache(int maxPages) {
		this.maxPages = maxPages;
		this.pages = new HashMap<>();
		this.nonEmptyPages = new TreeSet<>();
	}
	
	@Override
	public int getPageCount() {
		return maxPages;
	}
	@Override
	public int getDefaultLoadedPagesCount() {
		return maxPages;
	}
	
	@Override
	public ClientChestPage getCachedPage(int page) {
		return pages.computeIfAbsent(page, key -> ClientChestPage.unloaded());
	}
	@Override
	public void cachePage(int page, ClientChestPage pageData) {
		pages.put(page, pageData);
		nonEmptyPages.add(page);
	}
	@Override
	public void cacheEmptyPage(int page) {
		pages.put(page, new ClientChestPage());
		nonEmptyPages.remove(page);
	}
	@Override
	public void discardPageCache(int page) {
		pages.remove(page);
		nonEmptyPages.remove(page);
	}
	
	@Override
	public int[] getNearestItems(int page) {
		SortedSet<Integer> prevSet = nonEmptyPages.headSet(page);
		SortedSet<Integer> nextSet = nonEmptyPages.tailSet(page + 1);
		
		int prev = (prevSet.isEmpty() ? -1 : prevSet.last());
		int next = (nextSet.isEmpty() ? -1 : nextSet.first());
		
		return new int[] {prev, next};
	}
	
	@Override
	public void transferTo(ClientChestPageCache cache) {
		int maxPages = cache.getPageCount();
		
		pages.forEach((page, pageData) -> {
			if (page >= maxPages)
				return;
			
			if (nonEmptyPages.contains(page))
				cache.cachePage(page, pageData);
			else
				cache.cacheEmptyPage(page);
		});
	}
	
}
