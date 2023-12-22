package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.ClientHandledScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;

@Mixin(ClientPlayNetworkHandler.class)
public class ReceivePacketMixin {
	
	@Inject(method = "onScreenHandlerSlotUpdate", at = @At(value = "HEAD"), cancellable = true)
	private void onScreenHandlerSlotUpdate(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo info) {
		if (!MainUtil.client.isOnThread())
			return;
		
		if (MainUtil.client.currentScreen instanceof ClientHandledScreen) {
			info.cancel();
			
			if (packet.getSlot() == 45)
				MainUtil.client.player.getInventory().setStack(40, packet.getStack());
			else if (packet.getSlot() < 9) {
				if (packet.getSlot() > 4)
					MainUtil.client.player.getInventory().armor.set(8 - packet.getSlot(), packet.getStack());
			} else
				MainUtil.client.player.getInventory().setStack(packet.getSlot() >= 36 ? packet.getSlot() - 36 : packet.getSlot(), packet.getStack());
		}
	}
	
}
