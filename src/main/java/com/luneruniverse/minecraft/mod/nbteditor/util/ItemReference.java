package com.luneruniverse.minecraft.mod.nbteditor.util;

import java.io.IOException;

import org.lwjgl.glfw.GLFW;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.containers.ContainerIO;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ClientChestScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ItemsScreen;

import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

/**
 * Refers to items in the players inventory, client chest, or container (possibly recursively)<br>
 * Use {@link #getItem()} and {@link #saveItem(ItemStack, Runnable)} to interface with the item
 */
public class ItemReference {
	
	private final Hand hand;
	private final int page;
	private final int slot;
	private final EquipmentSlot armorSlot;
	private final ItemReference parent;
	private final int hotbarSlot;
	private final SaveQueue saveQueue;
	
	public ItemReference(Hand hand) {
		this.hand = hand;
		this.page = -1;
		this.slot = -1;
		this.armorSlot = null;
		this.parent = null;
		this.hotbarSlot = hand == Hand.MAIN_HAND ? MainUtil.client.player.getInventory().selectedSlot : 40;
		
		this.saveQueue = new SaveQueue("Player Hand", (ItemStack toSave) -> {
			MainUtil.saveItem(hand, toSave);
		}, false);
	}
	
	public ItemReference(int slot) {
		this.hand = null;
		this.page = -1;
		this.slot = slot;
		this.armorSlot = null;
		this.parent = null;
		this.hotbarSlot = slot < 9 ? slot : (slot == 45 ? 40 : -1);
		
		this.saveQueue = new SaveQueue("Player Inventory", (ItemStack toSave) -> {
			MainUtil.saveItemInvSlot(slot, toSave);
		}, false);
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
		
		this.saveQueue = new SaveQueue("Player Armor", (ItemStack toSave) -> {
			MainUtil.saveItem(armorSlot, toSave);
		}, false);
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
		
		this.saveQueue = new SaveQueue("Client Chest", (ItemStack toSave) -> {
			ItemStack[] items = NBTEditorClient.CLIENT_CHEST.getPage(page);
			items[slot] = toSave;
			try {
				NBTEditorClient.CLIENT_CHEST.setPage(page, items);
				
				if (MainUtil.client.currentScreen instanceof ClientChestScreen && ClientChestScreen.PAGE == page)
					((ClientChestScreen) MainUtil.client.currentScreen).getScreenHandler().getSlot(slot).setStack(toSave);
			} catch (IOException e) {
				NBTEditor.LOGGER.error("Error while saving client chest", e);
				MainUtil.client.player.sendMessage(TextInst.translatable("nbteditor.client_chest.save_error"), false);
			}
		}, true);
	}
	
	public ItemReference(ItemReference parent, int slot) {
		this.hand = null;
		this.page = -1;
		this.slot = slot;
		this.armorSlot = null;
		this.parent = parent;
		this.hotbarSlot = -1;
		
		this.saveQueue = new SaveQueue("Container", (ItemStack toSave) -> {
			ItemStack chest = parent.getItem().copy();
			ItemStack[] contents = ContainerIO.read(chest);
			contents[slot] = toSave;
			ContainerIO.write(chest, contents);
			
			parent.saveQueue.save(chest);
			
			// The recursive nature causes parent containers to also write items to the screen, hence the check
			if (MainUtil.client.currentScreen instanceof ItemsScreen && ((ItemsScreen) MainUtil.client.currentScreen).getReference() == parent)
				((ItemsScreen) MainUtil.client.currentScreen).getScreenHandler().getSlot(slot).setStack(toSave);
		}, true);
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
		if (parent != null)
			return parent.getInvSlot();
		return -1;
	}
	public int getHotbarSlot() {
		if (hotbarSlot == -1 && parent != null)
			return parent.getHotbarSlot();
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
			return NBTEditorClient.CLIENT_CHEST.getPage(page)[slot];
		else if (isContainerReference())
			return ContainerIO.read(parent.getItem())[slot];
		else
			throw new IllegalStateException("Invalid item reference");
	}
	public void saveItem(ItemStack toSave, Runnable onFinished) {
		this.saveQueue.save(onFinished, toSave.copy());
	}
	
	public boolean isLocked() {
		if (isHandReference() || isInventoryReference() || isArmorReference())
			return false;
		else if (isClientChestReference())
			return ConfigScreen.isLockSlots();
		else if (isContainerReference())
			return parent.isLocked();
		else
			throw new IllegalStateException("Invalid item reference");
	}
	public boolean isLockable() {
		if (isHandReference() || isInventoryReference() || isArmorReference())
			return false;
		else if (isClientChestReference())
			return true;
		else if (isContainerReference())
			return parent.isLockable();
		else
			throw new IllegalStateException("Invalid item reference");
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
		else if (MultiVersionMisc.getInventoryKey(MainUtil.client.options).matchesKey(keyCode, scanCode))
			showParent();
		else
			return false;
		return true;
	}
	
}
