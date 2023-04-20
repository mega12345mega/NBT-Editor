package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.commands.get.GetLostItemCommand;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

@Mixin(HandledScreen.class)
public class HandledScreenMixin {
	@Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at = @At("HEAD"))
	private void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo info) {
		HandledScreen<?> source = (HandledScreen<?>) (Object) this;
		
		if (!source.getScreenHandler().getCursorStack().isEmpty())
			GetLostItemCommand.addToHistory(source.getScreenHandler().getCursorStack());
	}
}
