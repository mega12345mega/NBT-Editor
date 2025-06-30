package com.luneruniverse.minecraft.mod.nbteditor.util;

import net.minecraft.entity.EquipmentSlot;

/**
 * <h1>Slot Formats:</h1>
 * <h2>inv - Inventory (PlayerInventory):</h2>
 * <ul>
 *   <li>0-8: Hotbar</li>
 *   <li>9-35: Inventory</li>
 *   <li>36-39: Armor (feet -> head)</li>
 *   <li>40: Off Hand</li>
 * </ul>
 * <h2>container - Inventory Container (PlayerScreenHandler):</h2>
 * <ul>
 *   <li>5-8: Armor <strong>(head -> feet)</strong></li>
 *   <li>9-35: Inventory</li>
 *   <li>36-44: Hotbar</li>
 *   <li>45: Off Hand</li>
 * </ul>
 * <h2>generic - Player Inventory Part of Generic Container (GenericContainerScreenHandler):</h2>
 * These slot indices are meant to be added to the number of slots in the upper inventory
 * <ul>
 *   <li>0-26: Inventory</li>
 *   <li>27-35: Hotbar</li>
 *   <li>36-39: Armor (feet -> head) (not accessible)</li>
 *   <li>40: Off Hand (not visible)</li>
 * </ul>
 * <h2>generic container - All of Generic Container (GenericContainerScreenHandler)</h2>
 * The slot indices of the GenericContainerScreenHandler of size n:
 * <ul>
 *   <li>0-(n-1): Upper Inventory</li>
 *   <li>n-(n+40): Format: generic (see above)</li>
 * </ul>
 */
public class SlotUtil {
	
	// Conversions
	
	public static int invToContainer(int slot) {
		if (slot < 0)
			throw new IllegalArgumentException("Invalid slot index: " + slot);
		if (slot < 9)
			return slot + 36;
		if (slot < 36)
			return slot;
		if (slot < 40)
			return 8 - (slot - 36);
		if (slot == 40)
			return 45;
		throw new IllegalArgumentException("Invalid slot index: " + slot);
	}
	public static int invToGeneric(int slot) {
		if (slot < 0)
			throw new IllegalArgumentException("Invalid slot index: " + slot);
		if (slot < 9)
			return slot + 27;
		if (slot < 36)
			return slot - 9;
		if (slot <= 40)
			return slot;
		throw new IllegalArgumentException("Invalid slot index: " + slot);
	}
	
	public static int containerToInv(int slot) {
		if (slot < 5)
			throw new IllegalArgumentException("Invalid slot index: " + slot);
		if (slot < 9)
			return 8 - slot + 36;
		if (slot < 36)
			return slot;
		if (slot < 45)
			return slot - 36;
		if (slot == 45)
			return 40;
		throw new IllegalArgumentException("Invalid slot index: " + slot);
	}
	public static int containerToGeneric(int slot) {
		return invToGeneric(containerToInv(slot));
	}
	
	public static int genericToInv(int slot) {
		if (slot < 0)
			throw new IllegalArgumentException("Invalid slot index: " + slot);
		if (slot < 27)
			return slot + 9;
		if (slot < 36)
			return slot - 27;
		if (slot <= 40)
			return slot;
		throw new IllegalArgumentException("Invalid slot index: " + slot);
	}
	public static int genericToContainer(int slot) {
		return invToContainer(genericToInv(slot));
	}
	
	// Extractions (inv)
	
	public static boolean isHotbarFromInv(int slot) {
		return 0 <= slot && slot < 9;
	}
	public static int extractHotbarFromInv(int slot) {
		if (isHotbarFromInv(slot))
			return slot;
		throw new IllegalArgumentException("Invalid hotbar index: " + slot);
	}
	
	public static boolean isInventoryFromInv(int slot) {
		return 9 <= slot && slot < 36;
	}
	public static int extractInventoryFromInv(int slot) {
		if (isInventoryFromInv(slot))
			return slot - 9;
		throw new IllegalArgumentException("Invalid inventory index: " + slot);
	}
	
	public static boolean isArmorFromInv(int slot) {
		return 36 <= slot && slot < 40;
	}
	public static EquipmentSlot extractArmorFromInv(int slot) {
		if (isArmorFromInv(slot))
			return EquipmentSlot.values()[slot - 36 + 2];
		throw new IllegalArgumentException("Invalid armor index: " + slot);
	}
	
	public static boolean isOffHandFromInv(int slot) {
		return slot == 40;
	}
	
	// Extractions (container)
	
	public static boolean isHotbarFromContainer(int slot) {
		return isHotbarFromInv(containerToInv(slot));
	}
	public static int extractHotbarFromContainer(int slot) {
		return extractHotbarFromInv(containerToInv(slot));
	}
	
	public static boolean isInventoryFromContainer(int slot) {
		return isInventoryFromInv(containerToInv(slot));
	}
	public static int extractInventoryFromContainer(int slot) {
		return extractInventoryFromInv(containerToInv(slot));
	}
	
	public static boolean isArmorFromContainer(int slot) {
		return isArmorFromInv(containerToInv(slot));
	}
	public static EquipmentSlot extractArmorFromContainer(int slot) {
		return extractArmorFromInv(containerToInv(slot));
	}
	
	public static boolean isOffHandFromContainer(int slot) {
		return isOffHandFromInv(containerToInv(slot));
	}
	
	// Extractions (generic)
	
	public static boolean isHotbarFromGeneric(int slot) {
		return isHotbarFromInv(genericToInv(slot));
	}
	public static int extractHotbarFromGeneric(int slot) {
		return extractHotbarFromInv(genericToInv(slot));
	}
	
	public static boolean isInventoryFromGeneric(int slot) {
		return isInventoryFromInv(genericToInv(slot));
	}
	public static int extractInventoryFromGeneric(int slot) {
		return extractInventoryFromInv(genericToInv(slot));
	}
	
	public static boolean isArmorFromGeneric(int slot) {
		return isArmorFromInv(genericToInv(slot));
	}
	public static EquipmentSlot extractArmorFromGeneric(int slot) {
		return extractArmorFromInv(genericToInv(slot));
	}
	
	public static boolean isOffHandFromGeneric(int slot) {
		return isOffHandFromInv(genericToInv(slot));
	}
	
	// Creations (inv)
	
	public static int createHotbarInInv(int slot) {
		if (0 <= slot && slot < 9)
			return slot;
		throw new IllegalArgumentException("Invalid hotbar index: " + slot);
	}
	
	public static int createInventoryInInv(int slot) {
		if (0 <= slot && slot < 27)
			return slot + 9;
		throw new IllegalArgumentException("Invalid inventory index: " + slot);
	}
	
	public static int createArmorInInv(EquipmentSlot slot) {
		if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR)
			return slot.getEntitySlotId() + 36;
		throw new IllegalArgumentException("Invalid armor index: " + slot);
	}
	
	public static int createOffHandInInv() {
		return 40;
	}
	
	// Creations (container)
	
	public static int createHotbarInContainer(int slot) {
		return invToContainer(createHotbarInInv(slot));
	}
	
	public static int createInventoryInContainer(int slot) {
		return invToContainer(createInventoryInInv(slot));
	}
	
	public static int createArmorInContainer(EquipmentSlot slot) {
		return invToContainer(createArmorInInv(slot));
	}
	
	public static int createOffHandInContainer() {
		return invToContainer(createOffHandInInv());
	}
	
	// Creations (generic)
	
	public static int createHotbarInGeneric(int slot) {
		return invToGeneric(createHotbarInInv(slot));
	}
	
	public static int createInventoryInGeneric(int slot) {
		return invToGeneric(createInventoryInInv(slot));
	}
	
	public static int createArmorInGeneric(EquipmentSlot slot) {
		return invToGeneric(createArmorInInv(slot));
	}
	
	public static int createOffHandInGeneric() {
		return invToGeneric(createOffHandInInv());
	}
	
}
