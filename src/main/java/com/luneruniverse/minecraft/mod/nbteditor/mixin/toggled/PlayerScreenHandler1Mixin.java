package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMixinLink;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

@Mixin(targets = "net.minecraft.screen.PlayerScreenHandler$1")
public class PlayerScreenHandler1Mixin {
	@Inject(method = "method_7680(Lnet/minecraft/class_1799;)Z", at = @At("HEAD"), cancellable = true, remap = false)
	@SuppressWarnings("target")
	private void canInsert(ItemStack stack, CallbackInfoReturnable<Boolean> info) {
		ServerMixinLink.slotCanInsertOrTake((Slot) (Object) this, info, true);
	}
	@Inject(method = "method_7674(Lnet/minecraft/class_1657;)Z", at = @At("HEAD"), cancellable = true, remap = false)
	@SuppressWarnings("target")
	private void canTakeItems(PlayerEntity player, CallbackInfoReturnable<Boolean> info) {
		ServerMixinLink.slotCanInsertOrTake((Slot) (Object) this, info, true);
	}
}
