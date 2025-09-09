package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMVMisc;

import net.minecraft.network.state.PlayStateFactories;
import net.minecraft.server.network.ServerPlayNetworkHandler;

@Mixin(targets = "net.minecraft.network.state.PlayStateFactories$1")
public class PlayStateFactories1Mixin {
	@Redirect(method = "decode", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/state/PlayStateFactories$PacketCodecModifierContext;isInCreativeMode()Z"))
	private boolean decode_isInCreativeMode(PlayStateFactories.PacketCodecModifierContext context) {
		if (context instanceof ServerPlayNetworkHandler serverHandler && ServerMVMisc.hasPermissionLevel(serverHandler.player, 2))
			return true;
		return context.isInCreativeMode();
	}
}
