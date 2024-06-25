package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.commands.get.GetLostItemCommand;
import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;

import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

@Mixin(CreativeInventoryScreen.class)
public class CreativeInventoryScreenMixin {
	@Inject(method = "onMouseClick", at = @At(value = "HEAD"), cancellable = true)
	private void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo info) {
		if (slot != null) {
			if (slot instanceof CreativeInventoryScreen.CreativeSlot)
				slot = ((CreativeInventoryScreen.CreativeSlot) slot).slot;
		}
		
		MixinLink.onMouseClick((CreativeInventoryScreen) (Object) this, slot, slotId, button, actionType, info);
	}
	@Inject(method = "onMouseClick", at = @At(value = "RETURN"))
	private void onMouseClickReturn(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo info) {
		ItemStack cursor = ((CreativeInventoryScreen) (Object) this).getScreenHandler().getCursorStack();
		if (!cursor.isEmpty())
			GetLostItemCommand.addToHistory(cursor);
	}
	
	@Inject(method = "keyPressed", at = @At(value = "HEAD"), cancellable = true)
	private void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> info) {
		MixinLink.keyPressed((CreativeInventoryScreen) (Object) this, keyCode, scanCode, modifiers, info);
	}
}
