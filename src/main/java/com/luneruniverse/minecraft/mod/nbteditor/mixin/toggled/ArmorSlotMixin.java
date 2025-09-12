package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMixinLink;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

@Mixin(targets = "net.minecraft.screen.slot.ArmorSlot")
public class ArmorSlotMixin {
	@Shadow
	private @Final LivingEntity entity;
	
	@Inject(method = "canInsert", at = @At("HEAD"), cancellable = true)
	private void canInsert(ItemStack stack, CallbackInfoReturnable<Boolean> info) {
		if (entity instanceof PlayerEntity)
			ServerMixinLink.slotCanInsertOrTake((Slot) (Object) this, info, true);
		else if (entity instanceof AbstractHorseEntity)
			ServerMixinLink.slotCanInsertOrTake((Slot) (Object) this, info, false);
	}
	@Inject(method = "canTakeItems", at = @At("HEAD"), cancellable = true)
	private void canTakeItems(PlayerEntity player, CallbackInfoReturnable<Boolean> info) {
		if (entity instanceof PlayerEntity)
			ServerMixinLink.slotCanInsertOrTake((Slot) (Object) this, info, true);
		else if (entity instanceof AbstractHorseEntity)
			ServerMixinLink.slotCanInsertOrTake((Slot) (Object) this, info, false);
	}
}
