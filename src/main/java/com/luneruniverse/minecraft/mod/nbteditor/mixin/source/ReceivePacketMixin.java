package com.luneruniverse.minecraft.mod.nbteditor.mixin.source;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.screens.ClientContainerScreen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;

@Mixin(ClientConnection.class)
public class ReceivePacketMixin {
	
	@SuppressWarnings("resource")
	@Inject(method = "handlePacket", at = @At(value = "HEAD"), cancellable = true)
	private static <T extends PacketListener> Packet<T> onReceivePacket(Packet<T> packet, PacketListener listener, CallbackInfo info) {
		if (MinecraftClient.getInstance().currentScreen instanceof ClientContainerScreen) {
			if (packet instanceof ScreenHandlerSlotUpdateS2CPacket) {
				info.cancel();
				
				ScreenHandlerSlotUpdateS2CPacket castedPacket = (ScreenHandlerSlotUpdateS2CPacket) packet;
				if (castedPacket.getSlot() == 45)
					MinecraftClient.getInstance().player.getInventory().setStack(45, castedPacket.getItemStack());
				else
					MinecraftClient.getInstance().player.getInventory().setStack(castedPacket.getSlot() >= 36 ? castedPacket.getSlot() - 36 : castedPacket.getSlot(), castedPacket.getItemStack());
			}
		}
		
		return packet;
	}
	
}
