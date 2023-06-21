package com.luneruniverse.minecraft.mod.nbteditor.itemreferences;

import org.lwjgl.glfw.GLFW;

import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public interface ItemReference {
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
