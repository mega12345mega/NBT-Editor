package com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences;

import java.lang.reflect.Proxy;
import java.util.function.Predicate;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalItem;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalItemStack;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalNBT;
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
			return getInventoryOrArmorItem(slotObj.getIndex(), false).setParent(screen);
		}
		return new ServerItemReference(slot, screen);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends LocalNBT> NBTReference<T> toItemStackRef(NBTReference<T> ref) {
		if (ref instanceof ItemReference itemRef)
			return (NBTReference<T>) itemRef.toStackRef();
		return ref;
	}
	/**
	 * @see #toPartsRef() for deprecation details
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
	public static <T extends LocalNBT> NBTReference<T> toItemPartsRef(NBTReference<T> ref) {
		if (ref instanceof ItemReference itemRef)
			return (NBTReference<T>) itemRef.toPartsRef();
		return ref;
	}
	
	public default ItemReference toStackRef() {
		return this;
	}
	/**
	 * Make sure to call {@link #toStackRef()} before passing this to any code not designed for a parts ref!<br>
	 * Also, make sure to never call {@link LocalItem#getEditableItem()}!
	 */
	@Deprecated
	public default ItemReference toPartsRef() {
		ItemReference stackRef = this;
		return (ItemReference) Proxy.newProxyInstance(ItemReference.class.getClassLoader(),
				new Class<?>[] {ItemReference.class}, (obj, method, args) -> {
			if (method.getName().equals("toStackRef")) {
				return stackRef;
			}
			if (method.getName().equals("toPartsRef")) {
				return obj;
			}
			
			Object output = method.invoke(stackRef, args);
			if (output instanceof LocalItem localItem)
				return localItem.toParts();
			return output;
		});
	}
	
	@Override
	public default LocalItem getLocalNBT() {
		return new LocalItemStack(getItem());
	}
	@Override
	public default void saveLocalNBT(LocalItem nbt, Runnable onFinished) {
		saveItem(nbt.getReadableItem(), onFinished);
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
		NbtCompound nbt = getItem().manager$getNbt();
		if (nbt != null)
			return nbt;
		return new NbtCompound();
	}
	@Override
	public default void saveNBT(Identifier id, NbtCompound toSave, Runnable onFinished) {
		ItemStack item = getItem();
		if (!MVRegistry.ITEM.getId(item.getItem()).equals(id))
			item = MainUtil.setType(MVRegistry.ITEM.get(id), item);
		item.manager$setNbt(toSave);
		saveItem(item, onFinished);
	}
	
	@Override
	public void showParent();
}
