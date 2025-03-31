package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMixinLink;

import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
	@Shadow
	public ServerPlayerEntity player;
	
	@Redirect(method = "onCreativeInventoryAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerInteractionManager;isCreative()Z"))
	private boolean isCreative(ServerPlayerInteractionManager manager) {
		return manager.isCreative() || ServerMVMisc.hasPermissionLevel(player, 2);
	}
	
	@Inject(method = "onClickSlot", at = @At("HEAD"))
	private void onClickSlot(ClickSlotC2SPacket packet, CallbackInfo info) {
		ServerMixinLink.NO_SLOT_RESTRICTIONS_PLAYERS.put(player, packet.isNoSlotRestrictions());
	}
}
