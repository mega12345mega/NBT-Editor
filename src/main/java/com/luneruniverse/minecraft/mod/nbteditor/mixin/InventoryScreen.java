package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.containers.ContainerIO;
import com.luneruniverse.minecraft.mod.nbteditor.mixin.source.HandledScreenAccessor;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ClientChestScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.CreativeTab;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ItemsScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

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
				if (cursor.getItem() != Items.ENCHANTED_BOOK) { // Make sure the cursor is an enchanted book
					ItemStack temp = cursor;
					cursor = item;
					item = temp;
				}
				
				slotId = slot.id;
				boolean armor = false;
				if (source instanceof CreativeInventoryScreen && !MultiVersionMisc.isCreativeInventoryTabSelected())
					slotId -= 9;
				else if (slotId < 9)
					armor = true;
				
				MainUtil.addEnchants(EnchantmentHelper.get(cursor), item);
				if (armor)
					MainUtil.saveItem(EquipmentSlot.fromTypeIndex(EquipmentSlot.Type.ARMOR, 8 - slotId), item);
				else
					MainUtil.saveItemInvSlot(slotId, item);
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
			if (hoveredSlot != null && hoveredSlot.inventory == MainUtil.client.player.getInventory() && (ConfigScreen.isAirEditable() || hoveredSlot.getStack() != null && !hoveredSlot.getStack().isEmpty())) {
				int slot = hoveredSlot.getIndex();
				if (source instanceof CreativeInventoryScreen && !MultiVersionMisc.isCreativeInventoryTabSelected())
					slot += 36;
				ItemReference ref = slot < 9 ? ItemReference.getArmorFromSlot(slot) : new ItemReference(slot == 45 ? 45 : (slot < 9 ? slot + 54 : (slot >= 36 ? slot - 36 : slot)));
				if (Screen.hasControlDown()) {
					if (hoveredSlot.getStack() != null && ContainerIO.isContainer(hoveredSlot.getStack()))
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
		source.addDrawableChild(new CreativeTab(source, new ItemStack(Items.ENDER_CHEST).setCustomName(TextInst.translatable("itemGroup.nbteditor.client_chest")), ClientChestScreen::show));
	}
}
