package com.luneruniverse.minecraft.mod.nbteditor.itemreferences;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.ClientChestScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.luneruniverse.minecraft.mod.nbteditor.util.SaveQueue;

import net.minecraft.item.ItemStack;

public class ClientChestItemReference implements ItemReference {
	
	private final int page;
	private final int slot;
	private final SaveQueue save;
	
	public ClientChestItemReference(int page, int slot) {
		this.page = page;
		this.slot = slot;
		
		this.save = new SaveQueue("Client Chest", (ItemStack toSave) -> {
			ItemStack[] items = NBTEditorClient.CLIENT_CHEST.getPage(page);
			items[slot] = toSave;
			try {
				NBTEditorClient.CLIENT_CHEST.setPage(page, items);
				
				if (MainUtil.client.currentScreen instanceof ClientChestScreen && ClientChestScreen.PAGE == page)
					((ClientChestScreen) MainUtil.client.currentScreen).getScreenHandler().getSlot(slot).setStackNoCallbacks(toSave);
			} catch (Exception e) {
				NBTEditor.LOGGER.error("Error while saving client chest", e);
				MainUtil.client.player.sendMessage(TextInst.translatable("nbteditor.client_chest.save_error"), false);
			}
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
		return NBTEditorClient.CLIENT_CHEST.getPage(page)[slot];
	}
	
	@Override
	public void saveItem(ItemStack toSave, Runnable onFinished) {
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
	public void showParent() {
		ClientChestScreen.show();
	}
	
}
