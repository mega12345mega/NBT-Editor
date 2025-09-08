package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMixinLink;

import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
	@Shadow
	public ServerPlayerEntity player;
	
	@Redirect(method = "onCreativeInventoryAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;isInCreativeMode()Z"))
	@Group(name = "onCreativeInventoryAction_isInCreativeMode", min = 1)
	private boolean onCreativeInventoryAction_isInCreativeMode(ServerPlayerEntity player) {
		return player.isInCreativeMode() || ServerMVMisc.hasPermissionLevel(player, 2);
	}
	@Redirect(method = "method_12070(Lnet/minecraft/class_2873;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_3225;method_14268()Z"), remap = false)
	@Group(name = "onCreativeInventoryAction_isInCreativeMode", min = 1)
	@SuppressWarnings("target")
	private boolean onCreativeInventoryAction_isCreative(ServerPlayerInteractionManager manager) {
		return manager.isCreative() || ServerMVMisc.hasPermissionLevel(player, 2);
	}
	
	@Inject(method = "onClickSlot", at = @At("HEAD"))
	private void onClickSlot(ClickSlotC2SPacket packet, CallbackInfo info) {
		ServerMixinLink.NO_SLOT_RESTRICTIONS_PLAYERS.put(player, packet.isNoSlotRestrictions());
	}
	
	@Inject(method = "onCloseHandledScreen", at = @At("RETURN"))
	private void onCloseHandledScreen(CloseHandledScreenC2SPacket packet, CallbackInfo info) {
		// In singleplayer, paused screens will delay sending the updated cursor (air) to the client
		// This forces the updated cursor to be sent anyway
		player.currentScreenHandler.sendContentUpdates();
	}
}
