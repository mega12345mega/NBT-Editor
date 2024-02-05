package com.luneruniverse.minecraft.mod.nbteditor;

import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.packets.ContainerScreenS2CPacket;
import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.ClientChestScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.ContainerScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.fabricmc.fabric.api.client.networking.v1.C2SPlayChannelEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;

public class NBTEditorServerConn implements ClientPlayConnectionEvents.Init, ClientPlayConnectionEvents.Disconnect, C2SPlayChannelEvents.Register {
	
	public enum Status {
		DISCONNECTED,
		CLIENT_ONLY,
		BOTH
	}
	
	private Status status;
	private boolean containerScreen;
	
	public NBTEditorServerConn() {
		status = Status.DISCONNECTED;
		
		ClientPlayConnectionEvents.INIT.register(this);
		ClientPlayConnectionEvents.DISCONNECT.register(this);
		C2SPlayChannelEvents.REGISTER.register(this);
	}
	
	public Status getStatus() {
		return status;
	}
	public boolean isEditingExpanded() {
		if (status != Status.BOTH)
			return false;
		GameMode gameMode = MainUtil.client.interactionManager.getCurrentGameMode();
		return (gameMode.isCreative() || gameMode.isSurvivalLike()) && MainUtil.client.player.hasPermissionLevel(2);
	}
	public boolean isEditingAllowed() {
		return MainUtil.client.interactionManager.getCurrentGameMode().isCreative() || isEditingExpanded();
	}
	
	public boolean isScreenEditable() {
		Screen screen = MainUtil.client.currentScreen;
		return screen instanceof CreativeInventoryScreen ||
				screen instanceof ClientChestScreen ||
				screen instanceof ContainerScreen ||
				isEditingExpanded() && (screen instanceof InventoryScreen || containerScreen);
	}
	public void closeContainerScreen() {
		containerScreen = false;
	}
	public boolean isContainerScreen() {
		return containerScreen;
	}
	
	@Override
	public void onPlayInit(ClientPlayNetworkHandler network, MinecraftClient client) {
		status = Status.CLIENT_ONLY;
		ClientPlayNetworking.registerReceiver(ContainerScreenS2CPacket.TYPE, this::onContainerScreenPacket);
	}
	
	@Override
	public void onPlayDisconnect(ClientPlayNetworkHandler network, MinecraftClient client) {
		status = Status.DISCONNECTED;
	}
	
	@Override
	public void onChannelRegister(ClientPlayNetworkHandler network, PacketSender sender,
			MinecraftClient client, List<Identifier> channels) {
		if (channels.stream().anyMatch(channel -> channel.getNamespace().equals("nbteditor")))
			status = Status.BOTH;
	}
	
	private void onContainerScreenPacket(ContainerScreenS2CPacket packet, ClientPlayerEntity player, PacketSender sender) {
		containerScreen = true;
	}
	
}
