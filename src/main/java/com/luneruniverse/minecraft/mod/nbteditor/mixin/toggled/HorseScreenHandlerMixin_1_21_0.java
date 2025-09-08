package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

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
public class HorseScreenHandlerMixin_1_21_0 {
	@Inject(method = "<init>(ILnet/minecraft/class_1661;Lnet/minecraft/class_1263;Lnet/minecraft/class_1496;I)V", at = @At("HEAD"), remap = false)
	@SuppressWarnings("target")
	private static void initHead(int syncId, PlayerInventory playerInventory, Inventory inventory, AbstractHorseEntity horse, int slotColumnCount, CallbackInfo info) {
		ServerMixinLink.SCREEN_HANDLER_OWNER.put(Thread.currentThread(), playerInventory.player);
	}
	
	@Inject(method = "<init>(ILnet/minecraft/class_1661;Lnet/minecraft/class_1263;Lnet/minecraft/class_1496;I)V", at = @At("RETURN"), remap = false)
	@SuppressWarnings("target")
	private void initReturn(int syncId, PlayerInventory playerInventory, Inventory inventory, AbstractHorseEntity horse, int slotColumnCount, CallbackInfo info) {
		ServerMixinLink.SCREEN_HANDLER_OWNER.remove(Thread.currentThread());
	}
}
