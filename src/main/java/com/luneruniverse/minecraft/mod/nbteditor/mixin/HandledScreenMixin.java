package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.commands.get.GetLostItemCommand;
import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.ClientHandledScreen;

import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

@Mixin(HandledScreen.class)
public class HandledScreenMixin {
	@Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at = @At("HEAD"), cancellable = true)
	private void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo info) {
		if ((HandledScreen<?>) (Object) this instanceof ClientHandledScreen)
			return;
		MixinLink.onMouseClick((HandledScreen<?>) (Object) this, slot, slotId, button, actionType, info);
	}
	@Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at = @At("RETURN"))
	private void onMouseClickReturn(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo info) {
		if ((HandledScreen<?>) (Object) this instanceof ClientHandledScreen)
			return;
		ItemStack cursor = ((HandledScreen<?>) (Object) this).getScreenHandler().getCursorStack();
		if (!cursor.isEmpty())
			GetLostItemCommand.addToHistory(cursor);
	}
	
	@Inject(method = "keyPressed", at = @At(value = "HEAD"), cancellable = true)
	private void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> info) {
		HandledScreen<?> source = (HandledScreen<?>) (Object) this;
		if (source instanceof CreativeInventoryScreen || source instanceof ClientHandledScreen)
			return;
		MixinLink.keyPressed(source, keyCode, scanCode, modifiers, info);
	}
}
