package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMixinLink;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ShulkerBoxScreenHandler;

@Mixin(ShulkerBoxScreenHandler.class)
public class ShulkerBoxScreenHandlerMixin {
	@Inject(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/inventory/Inventory;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Inventory;onOpen(Lnet/minecraft/entity/player/PlayerEntity;)V"))
	private void initHead(int syncId, PlayerInventory playerInventory, Inventory inventory, CallbackInfo info) {
		ServerMixinLink.SCREEN_HANDLER_OWNER.put(Thread.currentThread(), playerInventory.player);
	}
	@Inject(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/inventory/Inventory;)V", at = @At("RETURN"))
	private void initReturn(int syncId, PlayerInventory playerInventory, Inventory inventory, CallbackInfo info) {
		ServerMixinLink.SCREEN_HANDLER_OWNER.remove(Thread.currentThread());
	}
}
