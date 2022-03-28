package com.luneruniverse.minecraft.mod.nbteditor.util;

import java.io.IOException;

import org.lwjgl.glfw.GLFW;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ClientChestScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ItemsScreen;

import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;

public class ItemReference {
	
	private final Hand hand;
	private final int page;
	private final int slot;
	private final EquipmentSlot armorSlot;
	private final ItemReference parent;
	private final int hotbarSlot;
	private final SaveQueue saveQueue;
	private volatile ItemStack toSave;
	
	public ItemReference(Hand hand) {
		this.hand = hand;
		this.page = -1;
		this.slot = -1;
		this.armorSlot = null;
		this.parent = null;
		this.hotbarSlot = hand == Hand.MAIN_HAND ? MainUtil.client.player.getInventory().selectedSlot : 40;
		
		this.saveQueue = new SaveQueue("Player Hand", () -> {
			MainUtil.saveItem(hand, toSave);
		});
	}
	
	public ItemReference(int slot) {
		this.hand = null;
		this.page = -1;
		this.slot = slot;
		this.armorSlot = null;
		this.parent = null;
		this.hotbarSlot = slot < 9 ? slot : (slot == 45 ? 40 : -1);
		
		this.saveQueue = new SaveQueue("Player Inventory", () -> {
			MainUtil.saveItemInvSlot(slot, toSave);
		});
	}
	
	public ItemReference(EquipmentSlot armorSlot) {
		if (armorSlot.getType() != EquipmentSlot.Type.ARMOR)
			throw new IllegalArgumentException("Invalid armor slot");
		
		this.hand = null;
		this.page = -1;
		this.slot = -1;
		this.armorSlot = armorSlot;
		this.parent = null;
		this.hotbarSlot = -1;
		
		this.saveQueue = new SaveQueue("Player Armor", () -> {
			MainUtil.saveItem(armorSlot, toSave);
		});
	}
	public static ItemReference getArmorFromSlot(int slot) {
		switch (slot) {
			case 5:
				return new ItemReference(EquipmentSlot.HEAD);
			case 6:
				return new ItemReference(EquipmentSlot.CHEST);
			case 7:
				return new ItemReference(EquipmentSlot.LEGS);
			case 8:
				return new ItemReference(EquipmentSlot.FEET);
			default:
				return null;
		}
	}
	
	public ItemReference(int page, int slot) {
		this.hand = null;
		this.page = page;
		this.slot = slot;
		this.armorSlot = null;
		this.parent = null;
		this.hotbarSlot = -1;
		
		this.saveQueue = new SaveQueue("Client Chest", () -> {
			ItemStack item = toSave; // Thread safe
			
			ItemStack[] items = NBTEditorClient.getClientChestPage(page);
			items[slot] = item;
			try {
				NBTEditorClient.setClientChestPage(page, items);
				
				if (MainUtil.client.currentScreen instanceof ClientChestScreen)
					((ClientChestScreen) MainUtil.client.currentScreen).getScreenHandler().getSlot(slot).setStack(item);
			} catch (IOException e) {
				e.printStackTrace();
				MainUtil.client.player.sendMessage(new TranslatableText("nbteditor.storage_save_error"), false);
			}
		});
	}
	
	public ItemReference(ItemReference parent, int slot) {
		this.hand = null;
		this.page = -1;
		this.slot = slot;
		this.armorSlot = null;
		this.parent = parent;
		this.hotbarSlot = -1;
		
		this.saveQueue = new SaveQueue("Container", () -> {
			ItemStack item = toSave;
			
			ItemChest chest = new ItemChest(parent.getItem().copy());
			chest.setStack(slot, item);
			parent.toSave = chest.getItem();
			parent.saveQueue.getOnSave().run();
			
			// The recursive nature causes parent containers to also write items to the screen, hence the check
			if (MainUtil.client.currentScreen instanceof ItemsScreen && ((ItemsScreen) MainUtil.client.currentScreen).getReference() == parent)
				((ItemsScreen) MainUtil.client.currentScreen).getScreenHandler().getSlot(slot).setStack(item);
		});
	}
	
	public boolean isHandReference() {
		return hand != null && parent == null && armorSlot == null && page == -1;
	}
	public boolean isInventoryReference() {
		return hand == null && parent == null && armorSlot == null && page == -1;
	}
	public boolean isArmorReference() {
		return hand == null && parent == null && armorSlot != null && page == -1;
	}
	public boolean isClientChestReference() {
		return hand == null && parent == null && armorSlot == null && page != -1;
	}
	public boolean isContainerReference() {
		return hand == null && parent != null && armorSlot == null && page == -1;
	}
	
	public Hand getHand() {
		return hand;
	}
	public int getSlot() {
		return slot;
	}
	public int getInvSlot() {
		if (isHandReference() && getHand() == Hand.MAIN_HAND || isInventoryReference())
			return slot < 9 ? slot + 27 : slot - 9;
		return -1;
	}
	public int getHotbarSlot() {
		return hotbarSlot;
	}
	
	public ItemStack getItem() {
		if (isHandReference())
			return MainUtil.client.player.getStackInHand(hand);
		else if (isInventoryReference())
			return slot == 45 ? MainUtil.client.player.getOffHandStack() : MainUtil.client.player.getInventory().getStack(slot);
		else if (isArmorReference())
			return MainUtil.client.player.getEquippedStack(armorSlot);
		else if (isClientChestReference())
			return NBTEditorClient.getClientChestPage(page)[slot];
		else if (isContainerReference())
			return new ItemChest(parent.getItem()).getStack(slot);
		else
			throw new IllegalStateException("Invalid item reference");
	}
	public void saveItem(ItemStack toSave, Runnable onFinished) {
		this.toSave = toSave.copy();
		this.saveQueue.save(onFinished);
	}
	
	public void showParent() {
		if (isHandReference())
			MainUtil.client.setScreen(null);
		else if (isInventoryReference() || isArmorReference())
			MainUtil.client.setScreen(new InventoryScreen(MainUtil.client.player));
		else if (isClientChestReference())
			ClientChestScreen.show();
		else if (isContainerReference())
			ItemsScreen.show(parent);
		else
			throw new IllegalStateException("Invalid item reference");
	}
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ESCAPE)
			MainUtil.client.setScreen(null);
		else if (MainUtil.client.options.keyInventory.matchesKey(keyCode, scanCode))
			showParent();
		else
			return false;
		return true;
	}
	
}
