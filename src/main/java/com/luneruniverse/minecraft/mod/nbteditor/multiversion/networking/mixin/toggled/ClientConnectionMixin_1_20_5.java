package com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.mixin.toggled;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVClientNetworking;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVServerNetworking;
import com.luneruniverse.minecraft.mod.nbteditor.server.ClientLink;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.server.network.ServerPlayNetworkHandler;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin_1_20_5 {
	@Shadow
	private NetworkSide side;
	@Shadow
	private PacketListener packetListener;
	
	private PacketListener prevListener;
	
	@Inject(method = "setPacketListener", at = @At("HEAD"))
	private void setPacketListener_head(NetworkState<?> state, PacketListener listener, CallbackInfo info) {
		prevListener = packetListener;
	}
	
	@Inject(method = "transitionInbound", at = @At("RETURN"))
	private void transitionInbound_return(NetworkState<?> state, PacketListener listener, CallbackInfo info) {
		if (side == NetworkSide.CLIENTBOUND) {
			if (ClientLink.isInstanceOfClientPlayNetworkHandler(listener))
				MVClientNetworking.PlayNetworkStateEvents.Start.EVENT.invoker().onPlayStart();
			else if (ClientLink.isInstanceOfClientPlayNetworkHandler(prevListener))
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
}
