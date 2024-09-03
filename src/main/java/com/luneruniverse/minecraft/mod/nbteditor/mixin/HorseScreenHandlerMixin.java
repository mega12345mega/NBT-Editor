package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMixinLink;

import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.HorseScreenHandler;

@Mixin(HorseScreenHandler.class)
public class HorseScreenHandlerMixin {
	@Inject(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/entity/passive/AbstractHorseEntity;I)V", at = @At("HEAD"), require = 0)
	private static void initHead(int syncId, PlayerInventory playerInventory, Inventory inventory, AbstractHorseEntity horse, int slotColumnCount, CallbackInfo info) {
		ServerMixinLink.SCREEN_HANDLER_OWNER.put(Thread.currentThread(), playerInventory.player);
	}
	
	@Inject(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/entity/passive/AbstractHorseEntity;I)V", at = @At("RETURN"), require = 0)
	private void initReturn(int syncId, PlayerInventory playerInventory, Inventory inventory, AbstractHorseEntity horse, int slotColumnCount, CallbackInfo info) {
		ServerMixinLink.SCREEN_HANDLER_OWNER.remove(Thread.currentThread());
	}
}
