package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.item.ItemStack;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
	@Shadow
	public ServerPlayerEntity player;
	@Redirect(method = "onCreativeInventoryAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerInteractionManager;isCreative()Z"))
	private boolean isCreative(ServerPlayerInteractionManager manager) {
		return manager.isCreative() || player.hasPermissionLevel(2);
	}
	@Redirect(method = "onCreativeInventoryAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isItemEnabled(Lnet/minecraft/resource/featuretoggle/FeatureSet;)Z"))
	private boolean isItemEnabled(ItemStack item, FeatureSet features) {
		if (player.hasPermissionLevel(2))
			return true;
		return item.isItemEnabled(features);
	}
}
