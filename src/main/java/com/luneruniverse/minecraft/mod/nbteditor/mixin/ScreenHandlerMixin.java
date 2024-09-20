package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.ClientHandledScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin {
	@Redirect(method = "internalOnSlotClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;dropItem(Lnet/minecraft/item/ItemStack;Z)Lnet/minecraft/entity/ItemEntity;"))
	private ItemEntity dropItem(PlayerEntity player, ItemStack stack, boolean retainOwnership) {
		if (!(MainUtil.client.currentScreen instanceof ClientHandledScreen))
			return player.dropItem(stack, retainOwnership);
		
		MainUtil.dropCreativeStack(stack);
		return null;
	}
}
