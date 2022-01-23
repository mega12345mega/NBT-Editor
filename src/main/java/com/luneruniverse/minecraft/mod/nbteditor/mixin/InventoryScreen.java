package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.mixin.source.HandledScreenAccessor;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ClientChestScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.CreativeTab;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ItemsScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.TranslatableText;

public class InventoryScreen {
	public static void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType, HandledScreen<?> source, CallbackInfo info) {
		if (!ctrlDown)
			return;
		
		if (actionType == SlotActionType.PICKUP && slot != null && slot.inventory == MainUtil.client.player.getInventory()) {
			ItemStack cursor = source.getScreenHandler().getCursorStack();
			ItemStack item = slot.getStack();
			if (cursor == null || cursor.isEmpty() || item == null || item.isEmpty())
				return;
			if (cursor.getItem() == Items.ENCHANTED_BOOK || item.getItem() == Items.ENCHANTED_BOOK) {
				if (cursor.getItem() != Items.ENCHANTED_BOOK) { // Make the cursor always be an enchanted book
					ItemStack temp = cursor;
					cursor = item;
					item = temp;
				}
				
				EnchantmentHelper.get(cursor).forEach(item::addEnchantment);
				MainUtil.saveItemInvSlot(slot.id, item);
				source.getScreenHandler().setCursorStack(ItemStack.EMPTY);
				
				info.cancel();
			}
		}
	}
	
	private static boolean ctrlDown;
	public static void keyPressed(int keyCode, int scanCode, int modifiers, HandledScreen<?> source, CallbackInfoReturnable<Boolean> info) {
		if (keyCode == GLFW.GLFW_KEY_LEFT_CONTROL || keyCode == GLFW.GLFW_KEY_RIGHT_CONTROL)
			ctrlDown = true;
		
		if (keyCode == GLFW.GLFW_KEY_SPACE) {
			Slot hoveredSlot = ((HandledScreenAccessor) source).getFocusedSlot();
			if (hoveredSlot != null && hoveredSlot.inventory == MainUtil.client.player.getInventory() && hoveredSlot.getStack() != null && !hoveredSlot.getStack().isEmpty()) {
				int slot = hoveredSlot.getIndex();
				ItemReference ref = slot < 9 ? ItemReference.getArmorFromSlot(slot) : new ItemReference(slot == 45 ? 45 : (slot < 9 ? slot + 54 : (slot >= 36 ? slot - 36 : slot)));
				if (Screen.hasControlDown()) {
					if (ItemsScreen.isContainer(hoveredSlot.getStack()))
						ItemsScreen.show(ref);
				} else
					MainUtil.client.setScreen(new NBTEditorScreen(ref));
			}
		}
	}
	public static void keyReleased(int keyCode, int scanCode, int modifiers, HandledScreen<?> source, CallbackInfoReturnable<Boolean> info) {
		if (keyCode == GLFW.GLFW_KEY_LEFT_CONTROL || keyCode == GLFW.GLFW_KEY_RIGHT_CONTROL)
			ctrlDown = false;
	}
	
	
	public static void init(CreativeInventoryScreen source, CallbackInfo info) {
		source.addDrawableChild(new CreativeTab(source, new ItemStack(Items.ENDER_CHEST).setCustomName(new TranslatableText("itemGroup.nbteditor.client_chest")), ClientChestScreen::show));
	}
}
