package com.luneruniverse.minecraft.mod.nbteditor.addons.events;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.ActionResult;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.fabricmc.fabric.api.event.Event;
import net.minecraft.item.ItemStack;

/**
 * Callback for when an item is detected to be lost<br>
 * Called before the lost item is set and the message is sent to the player
 */
public interface ItemLostCallback {
	Event<ItemLostCallback> EVENT = MainUtil.newEvent(ItemLostCallback.class);
	
	/**
	 * @param lostItem The item that was lost
	 * @return What should be done next
	 */
	ActionResult onItemLost(ItemStack lostItem);
}
