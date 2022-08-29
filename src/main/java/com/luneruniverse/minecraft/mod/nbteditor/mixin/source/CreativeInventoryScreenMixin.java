package com.luneruniverse.minecraft.mod.nbteditor.mixin.source;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import com.luneruniverse.minecraft.mod.nbteditor.mixin.InventoryScreen;

import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
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
			InventoryScreen.onMouseClick(slot, slotId, button, actionType, (HandledScreen<?>) (Object) this, info);
		} catch (Exception e) {
			NBTEditor.LOGGER.error("Error while handling slot click", e);
		}
	}
	
	@Inject(at = @At(value = "HEAD"), method = "keyPressed", cancellable = true)
	private void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> info) {
		InventoryScreen.keyPressed(keyCode, scanCode, modifiers, (HandledScreen<?>) (Object) this, info);
	}
	@Inject(at = @At(value = "HEAD"), method = "keyReleased", cancellable = true)
	private void keyReleased(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> info) {
		InventoryScreen.keyReleased(keyCode, scanCode, modifiers, (HandledScreen<?>) (Object) this, info);
	}
	
	
	@Inject(at = @At(value = "HEAD"), method = "init", cancellable = true)
	private void render(CallbackInfo info) {
		InventoryScreen.init((CreativeInventoryScreen) (Object) this, info);
	}
}
