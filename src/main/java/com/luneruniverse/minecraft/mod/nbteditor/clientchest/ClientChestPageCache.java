package com.luneruniverse.minecraft.mod.nbteditor.clientchest;

public interface ClientChestPageCache {
	public int getPageCount();
	public int getDefaultLoadedPagesCount();
	
	public ClientChestPage getCachedPage(int page);
	public void cachePage(int page, ClientChestPage pageData);
	public void cacheEmptyPage(int page);
	public void discardPageCache(int page);
	
	public int[] getNearestItems(int page);
	
	public void transferTo(ClientChestPageCache cache);
}
