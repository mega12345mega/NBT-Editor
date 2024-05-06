package com.luneruniverse.minecraft.mod.nbteditor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.packets.ContainerScreenS2CPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.ProtocolVersionS2CPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.ResponsePacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.ViewBlockS2CPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.ViewEntityS2CPacket;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.ClientChestScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.ContainerScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.world.GameMode;

public class NBTEditorServerConn implements ClientPlayConnectionEvents.Init, ClientPlayConnectionEvents.Disconnect {
	
	public enum Status {
		DISCONNECTED,
		CLIENT_ONLY,
		INCOMPATIBLE,
		BOTH
	}
	
	public static final int PROTOCOL_VERSION = 0;
	
	private Status status;
	private boolean containerScreen;
	private int lastRequestId;
	private final Map<Integer, CompletableFuture<FabricPacket>> requests;
	
	public NBTEditorServerConn() {
		status = Status.DISCONNECTED;
		containerScreen = false;
		lastRequestId = -1;
		requests = new HashMap<>();
		
		ClientPlayConnectionEvents.INIT.register(this);
		ClientPlayConnectionEvents.DISCONNECT.register(this);
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
	
	public <T extends FabricPacket> CompletableFuture<Optional<T>> sendRequest(Function<Integer, FabricPacket> packet, Class<T> responseType) {
		if (!isEditingExpanded())
			return CompletableFuture.completedFuture(Optional.empty());
		CompletableFuture<FabricPacket> future = new CompletableFuture<>();
		int requestId = ++lastRequestId;
		requests.put(requestId, future);
		ClientPlayNetworking.send(packet.apply(requestId));
		return future.thenApply(response -> {
			if (responseType.isInstance(response))
				return Optional.of(responseType.cast(response));
			return Optional.<T>empty();
		}).completeOnTimeout(Optional.empty(), 1000, TimeUnit.MILLISECONDS).thenApply(output -> {
			requests.remove(requestId);
			return output;
		});
	}
	private void receiveRequest(ResponsePacket packet, ClientPlayerEntity player, PacketSender sender) {
		CompletableFuture<FabricPacket> receiver = requests.remove(packet.getRequestId());
		if (receiver != null)
			receiver.complete(packet);
	}
	
	@Override
	public void onPlayInit(ClientPlayNetworkHandler network, MinecraftClient client) {
		status = Status.CLIENT_ONLY;
		ClientPlayNetworking.registerReceiver(ProtocolVersionS2CPacket.TYPE, this::onProtocolVersionPacket);
		ClientPlayNetworking.registerReceiver(ContainerScreenS2CPacket.TYPE, this::onContainerScreenPacket);
		ClientPlayNetworking.registerReceiver(ViewBlockS2CPacket.TYPE, this::receiveRequest);
		ClientPlayNetworking.registerReceiver(ViewEntityS2CPacket.TYPE, this::receiveRequest);
	}
	
	@Override
	public void onPlayDisconnect(ClientPlayNetworkHandler network, MinecraftClient client) {
		status = Status.DISCONNECTED;
	}
	
	private void onProtocolVersionPacket(ProtocolVersionS2CPacket packet, ClientPlayerEntity player, PacketSender sender) {
		if (packet.getVersion() == PROTOCOL_VERSION)
			status = Status.BOTH;
		else {
			status = Status.INCOMPATIBLE;
			if (ConfigScreen.isWarnIncompatibleProtocol()) {
				MainUtil.client.getToastManager().add(new SystemToast(SystemToast.Type.PACK_LOAD_FAILURE,
						TextInst.translatable("nbteditor.incompatible_protocol.title"),
						TextInst.translatable("nbteditor.incompatible_protocol.desc")));
			}
		}
	}
	
	private void onContainerScreenPacket(ContainerScreenS2CPacket packet, ClientPlayerEntity player, PacketSender sender) {
		containerScreen = true;
	}
	
}
