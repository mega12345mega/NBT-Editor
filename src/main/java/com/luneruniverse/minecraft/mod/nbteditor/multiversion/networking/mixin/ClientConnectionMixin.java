package com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVClientNetworking;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVPacket;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVServerNetworking;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.text.Text;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {
	@Shadow
	private NetworkSide side;
	@Shadow
	private PacketListener packetListener;
	@Shadow
	public boolean isOpen() { return false; }
	
	private PacketListener prevListener;
	
	@Inject(method = "setPacketListener", at = @At("HEAD"))
	private void setPacketListener_head(PacketListener listener, CallbackInfo info) {
		prevListener = packetListener;
	}
	@Inject(method = "setPacketListener", at = @At("RETURN"))
	private void setPacketListener_return(PacketListener listener, CallbackInfo info) {
		if (side == NetworkSide.CLIENTBOUND) {
			if (listener instanceof ClientPlayNetworkHandler)
				MVClientNetworking.PlayNetworkStateEvents.Start.EVENT.invoker().onPlayStart();
			else if (prevListener instanceof ClientPlayNetworkHandler)
				MVClientNetworking.PlayNetworkStateEvents.Stop.EVENT.invoker().onPlayStop();
		}
		if (side == NetworkSide.SERVERBOUND) {
			if (listener instanceof ServerPlayNetworkHandler handler)
				MVServerNetworking.PlayNetworkStateEvents.Start.EVENT.invoker().onPlayStart(handler.player);
			else if (prevListener instanceof ServerPlayNetworkHandler handler)
				MVServerNetworking.PlayNetworkStateEvents.Stop.EVENT.invoker().onPlayStop(handler.player);
		}
		prevListener = null;
	}
	@Inject(method = "disconnect", at = @At("HEAD"))
	private void disconnect(Text reason, CallbackInfo info) {
		if (isOpen()) {
			if (packetListener instanceof ClientPlayNetworkHandler)
				MVClientNetworking.PlayNetworkStateEvents.Stop.EVENT.invoker().onPlayStop();
			if (packetListener instanceof ServerPlayNetworkHandler handler)
				MVServerNetworking.PlayNetworkStateEvents.Stop.EVENT.invoker().onPlayStop(handler.player);
		}
	}
	
	@Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
	private static void handlePacket(Packet<?> packet, PacketListener listener, CallbackInfo info) {
		if (listener instanceof ClientPlayNetworkHandler handler && packet instanceof CustomPayloadS2CPacket customPacket) {
			if (customPacket.payload() instanceof MVPacket mvPacket) {
				MVClientNetworking.callListeners(mvPacket);
				info.cancel();
			}
		}
		if (listener instanceof ServerPlayNetworkHandler handler && packet instanceof CustomPayloadC2SPacket customPacket) {
			if (customPacket.payload() instanceof MVPacket mvPacket) {
				MVServerNetworking.callListeners(mvPacket, handler.player);
				info.cancel();
			}
		}
	}
}
