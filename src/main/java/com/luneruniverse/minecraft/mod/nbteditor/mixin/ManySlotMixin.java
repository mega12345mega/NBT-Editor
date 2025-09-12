package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMixinLink;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.FurnaceFuelSlot;
import net.minecraft.screen.slot.FurnaceOutputSlot;
import net.minecraft.screen.slot.ShulkerBoxSlot;
import net.minecraft.screen.slot.Slot;

@Mixin(value = {ShulkerBoxSlot.class, FurnaceFuelSlot.class, FurnaceOutputSlot.class}, targets = {"net.minecraft.screen.BrewingStandScreenHandler$PotionSlot", "net.minecraft.screen.BrewingStandScreenHandler$IngredientSlot", "net.minecraft.screen.BrewingStandScreenHandler$FuelSlot"})
public class ManySlotMixin {
	@Inject(method = "canInsert(Lnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
	private void canInsert(ItemStack item, CallbackInfoReturnable<Boolean> info) {
		ServerMixinLink.slotCanInsertOrTake((Slot) (Object) this, info, false);
	}
}
