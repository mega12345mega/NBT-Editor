package com.luneruniverse.minecraft.mod.nbteditor.itemreferences;

import java.util.function.Predicate;

import org.lwjgl.glfw.GLFW;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

public interface ItemReference {
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
	public void showParent();
	public default boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ESCAPE)
			MainUtil.client.setScreen(null);
		else if (MainUtil.client.options.inventoryKey.matchesKey(keyCode, scanCode))
			showParent();
		else
			return false;
		return true;
	}
}
