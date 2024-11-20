package com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences;

import java.util.Optional;

import com.luneruniverse.minecraft.mod.nbteditor.clientchest.ClientChestHelper;
import com.luneruniverse.minecraft.mod.nbteditor.clientchest.PageLoadLevel;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.ClientChestScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.luneruniverse.minecraft.mod.nbteditor.util.SaveQueue;

import net.minecraft.item.ItemStack;

public class ClientChestItemReference implements ItemReference {
	
	private final int page;
	private final int slot;
	private final SaveQueue<ItemStack> save;
	
	public ClientChestItemReference(int page, int slot) {
		this.page = page;
		this.slot = slot;
		
		this.save = new SaveQueue<>("ClientChest/" + (page + 1) + "/" + slot, toSave -> {
			ClientChestHelper.getPage(page, PageLoadLevel.DYNAMIC_ITEMS).join().ifPresent(pageData -> {
				pageData.getItemsOrThrow()[slot] = toSave;
				pageData.dynamicItems().remove(slot);
				ClientChestHelper.setPage(page, pageData.items(), pageData.dynamicItems()).join();
			});
		}, true);
	}
	
	public int getPage() {
		return page;
	}
	public int getSlot() {
		return slot;
	}
	
	@Override
	public ItemStack getItem() {
		return ClientChestHelper.getPage(page, PageLoadLevel.DYNAMIC_ITEMS).join().orElseThrow().getItemsOrThrow()[slot];
	}
	
	@Override
	public void saveItem(ItemStack toSave, Runnable onFinished) {
		if (MainUtil.client.currentScreen instanceof ClientChestScreen && ClientChestScreen.PAGE == page)
			((ClientChestScreen) MainUtil.client.currentScreen).getScreenHandler().getSlot(slot).setStackNoCallbacks(toSave);
		save.save(onFinished, toSave.copy());
	}
	
	@Override
	public boolean isLocked() {
		return ConfigScreen.isLockSlots();
	}
	
	@Override
	public boolean isLockable() {
		return true;
	}
	
	@Override
	public int getBlockedInvSlot() {
		return -1;
	}
	
	@Override
	public int getBlockedHotbarSlot() {
		return -1;
	}
	
	@Override
	public void showParent(Optional<ItemStack> cursor) {
		ClientChestScreen.show(cursor);
	}
	
	@Override
	public void clearParentCursor() {}
	
}
