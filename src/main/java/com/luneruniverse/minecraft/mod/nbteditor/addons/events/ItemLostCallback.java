package com.luneruniverse.minecraft.mod.nbteditor.addons.events;

import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.fabricmc.fabric.api.event.Event;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;

/**
 * Callback for when an item is detected to be lost<br>
 * Called before the lost item is set and the message is sent to the player
 */
public interface ItemLostCallback {
	Event<ItemLostCallback> EVENT = MainUtil.newEvent(ItemLostCallback.class);
	
	/**
	 * Return values:<br>
	 * {@link ActionResult#SUCCESS} - Immediately continue with normal behavior<br>
	 * {@link ActionResult#PASS} - Continue calling callbacks<br>
	 * {@link ActionResult#FAIL} - Immediately cancel
	 * @param lostItem The item that was lost
	 * @return The action result from above
	 */
	ActionResult onItemLost(ItemStack lostItem);
}
