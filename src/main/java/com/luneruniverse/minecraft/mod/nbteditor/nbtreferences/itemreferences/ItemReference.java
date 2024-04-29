package com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences;

import java.util.function.Predicate;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalItem;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.NBTReference;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

public interface ItemReference extends NBTReference<LocalItem> {
	public static ItemReference getHeldItem(Predicate<ItemStack> isAllowed, Text failText) throws CommandSyntaxException {
		ItemStack item = MainUtil.client.player.getMainHandStack();
		Hand hand = Hand.MAIN_HAND;
		if (item == null || item.isEmpty() || !isAllowed.test(item)) {
			item = MainUtil.client.player.getOffHandStack();
			hand = Hand.OFF_HAND;
		}
		if (item == null || item.isEmpty() || !isAllowed.test(item))
			throw new SimpleCommandExceptionType(failText).create();
		
		return new HandItemReference(hand);
	}
	public static ItemReference getHeldItem() throws CommandSyntaxException {
		return getHeldItem(item -> true, TextInst.translatable("nbteditor.no_hand.no_item.to_edit"));
	}
	public static ItemReference getHeldItemAirable() {
		try {
			return getHeldItem();
		} catch (CommandSyntaxException e) {
			return new HandItemReference(Hand.MAIN_HAND);
		}
	}
	public static ItemReference getHeldAir() throws CommandSyntaxException {
		if (MainUtil.client.player.getMainHandStack().isEmpty())
			return new HandItemReference(Hand.MAIN_HAND);
		if (MainUtil.client.player.getOffHandStack().isEmpty())
			return new HandItemReference(Hand.OFF_HAND);
		throw new SimpleCommandExceptionType(TextInst.translatable("nbteditor.no_hand.all_item")).create();
	}
	
	public static HandledScreenItemReference getInventoryOrArmorItem(int slot, boolean creative) {
		if (creative) {
			if (slot < 9)
				return new ArmorItemReference(slot);
			return new InventoryItemReference(slot == 45 ? 45 : (slot >= 36 ? slot - 36 : slot));
		}
		if (slot == 40)
			return new InventoryItemReference(45);
		if (slot >= 36)
			return new ArmorItemReference(slot - 36);
		return new InventoryItemReference(slot);
	}
	public static ItemReference getContainerItem(int slot, HandledScreen<?> screen) {
		if (slot == -1)
			return new ServerItemReference(-1, screen); // "CursorItemReference"
		
		Slot slotObj = screen.getScreenHandler().getSlot(slot);
		if (slotObj.inventory == MainUtil.client.player.getInventory()) {
			return getInventoryOrArmorItem(slotObj.getIndex(), false).withParent(screen);
		}
		return new ServerItemReference(slot, screen);
	}
	
	@Override
	public default LocalItem getLocalNBT() {
		return new LocalItem(getItem());
	}
	@Override
	public default void saveLocalNBT(LocalItem nbt, Runnable onFinished) {
		saveItem(nbt.getItem(), onFinished);
	}
	
	public ItemStack getItem();
	public void saveItem(ItemStack toSave, Runnable onFinished);
	public default void saveItem(ItemStack toSave, Text msg) {
		saveItem(toSave, () -> MainUtil.client.player.sendMessage(msg, false));
	}
	public default void saveItem(ItemStack toSave) {
		saveItem(toSave, () -> {});
	}
	
	public boolean isLocked();
	public boolean isLockable();
	
	/**
	 * Prevents a slot from being clicked while open in a container screen
	 * @return The slot to block (0-26 inv, 27-35 hotbar) or -1 if no slot should be blocked
	 */
	public int getBlockedInvSlot();
	/**
	 * Prevents a slot from being swapped (slot keybind) while open in a container screen
	 * @return The slot to block (0-8 hotbar, 40 offhand) or -1 if no slot should be blocked
	 */
	public int getBlockedHotbarSlot();
	
	@Override
	public default Identifier getId() {
		return MVRegistry.ITEM.getId(getItem().getItem());
	}
	@Override
	public default NbtCompound getNBT() {
		NbtCompound nbt = getItem().getNbt();
		if (nbt != null)
			return nbt;
		return new NbtCompound();
	}
	@Override
	public default void saveNBT(Identifier id, NbtCompound toSave, Runnable onFinished) {
		ItemStack item = getItem();
		if (!MVRegistry.ITEM.getId(item.getItem()).equals(id))
			item = MainUtil.setType(MVRegistry.ITEM.get(id), item);
		item.setNbt(toSave);
		saveItem(item, onFinished);
	}
}
