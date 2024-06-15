package com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
	@Shadow
	public ServerPlayerEntity player;
	
	@Inject(method = "<init>(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/network/ClientConnection;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/server/network/ConnectedClientData;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;setPacketListener(Lnet/minecraft/network/listener/PacketListener;)V"))
	@Group(name = "<init>", min = 1)
	private void init(MinecraftServer server, ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo info) {
		this.player = player;
		player.networkHandler = (ServerPlayNetworkHandler) (Object) this;
	}
	@Inject(method = "<init>(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/class_2535;Lnet/minecraft/class_3222;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_2535;method_10763(Lnet/minecraft/class_2547;)V"), remap = false)
	@Group(name = "<init>", min = 1)
	@SuppressWarnings("target")
	private void init(MinecraftServer server, ClientConnection connection, ServerPlayerEntity player, CallbackInfo info) {
		this.player = player;
		player.networkHandler = (ServerPlayNetworkHandler) (Object) this;
	}
}
