package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import com.luneruniverse.minecraft.mod.nbteditor.commands.get.GetLostItemCommand;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionMisc;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ClientContainerScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

@Mixin(CreativeInventoryScreen.class)
public class CreativeInventoryScreenMixin {
	@Inject(at = @At(value = "HEAD"), method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", cancellable = true)
	private void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo info) {
		try {
			if (slot != null) {
				if (slot instanceof CreativeInventoryScreen.CreativeSlot)
					slot = ((CreativeInventoryScreen.CreativeSlot) slot).slot;
			}
			
			CreativeInventoryScreen source = (CreativeInventoryScreen) (Object) this;
			
			if (!source.getScreenHandler().getCursorStack().isEmpty())
				GetLostItemCommand.addToHistory(source.getScreenHandler().getCursorStack());
			
			if (!Screen.hasControlDown())
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
			
		} catch (Exception e) {
			NBTEditor.LOGGER.error("Error while handling slot click", e);
		}
	}
	
	@Inject(at = @At(value = "HEAD"), method = "keyPressed", cancellable = true)
	private void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> info) {
		CreativeInventoryScreen source = (CreativeInventoryScreen) (Object) this;
		
		if (keyCode == GLFW.GLFW_KEY_SPACE) {
			Slot hoveredSlot = ((HandledScreenAccessor) source).getFocusedSlot();
			if (hoveredSlot != null && hoveredSlot.inventory == MainUtil.client.player.getInventory() && (ConfigScreen.isAirEditable() || hoveredSlot.getStack() != null && !hoveredSlot.getStack().isEmpty())) {
				int slot = hoveredSlot.getIndex();
				if (source instanceof CreativeInventoryScreen && !MultiVersionMisc.isCreativeInventoryTabSelected())
					slot += 36;
				ItemReference ref = slot < 9 ? ItemReference.getArmorFromSlot(slot) : new ItemReference(slot == 45 ? 45 : (slot < 9 ? slot + 54 : (slot >= 36 ? slot - 36 : slot)));
				ClientContainerScreen.handleKeybind(hoveredSlot, ref);
				info.setReturnValue(true);
			}
		}
	}
}
