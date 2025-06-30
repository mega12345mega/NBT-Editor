package com.luneruniverse.minecraft.mod.nbteditor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVClientNetworking;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.ContainerScreenS2CPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.ProtocolVersionS2CPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.ResponsePacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.ViewBlockS2CPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.ViewEntityS2CPacket;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.ClientChestScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.ContainerScreen;
import com.luneruniverse.minecraft.mod.nbteditor.server.NBTEditorServer;
import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.world.GameMode;

public class NBTEditorServerConn implements MVClientNetworking.PlayNetworkStateEvents.Start, MVClientNetworking.PlayNetworkStateEvents.Stop {
	
	public enum Status {
		DISCONNECTED,
		CLIENT_ONLY,
		INCOMPATIBLE,
		BOTH
	}
	
	private Status status;
	private boolean containerScreen;
	private int lastRequestId;
	private final Map<Integer, CompletableFuture<MVPacket>> requests;
	
	public NBTEditorServerConn() {
		status = Status.DISCONNECTED;
		containerScreen = false;
		lastRequestId = -1;
		requests = new HashMap<>();
		
		MVClientNetworking.registerListener(ProtocolVersionS2CPacket.ID, this::onProtocolVersionPacket);
		MVClientNetworking.registerListener(ContainerScreenS2CPacket.ID, this::onContainerScreenPacket);
		MVClientNetworking.registerListener(ViewBlockS2CPacket.ID, this::receiveRequest);
		MVClientNetworking.registerListener(ViewEntityS2CPacket.ID, this::receiveRequest);
		
		MVClientNetworking.PlayNetworkStateEvents.Start.EVENT.register(this);
		MVClientNetworking.PlayNetworkStateEvents.Stop.EVENT.register(this);
	}
	
	public Status getStatus() {
		return status;
	}
	public boolean isEditingExpanded() {
		if (status != Status.BOTH)
			return false;
		GameMode gameMode = MainUtil.client.interactionManager.getCurrentGameMode();
		return (gameMode.isCreative() || gameMode.isSurvivalLike()) && ServerMVMisc.hasPermissionLevel(MainUtil.client.player, 2);
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
	
	public <T extends MVPacket> CompletableFuture<Optional<T>> sendRequest(Function<Integer, MVPacket> packet, Class<T> responseType) {
		if (!isEditingExpanded())
			return CompletableFuture.completedFuture(Optional.empty());
		CompletableFuture<MVPacket> future = new CompletableFuture<>();
		int requestId = ++lastRequestId;
		requests.put(requestId, future);
		MVClientNetworking.send(packet.apply(requestId));
		return future.thenApply(response -> {
			if (responseType.isInstance(response))
				return Optional.of(responseType.cast(response));
			return Optional.<T>empty();
		}).completeOnTimeout(Optional.empty(), 1000, TimeUnit.MILLISECONDS).thenApply(output -> {
			requests.remove(requestId);
			return output;
		});
	}
	private void receiveRequest(ResponsePacket packet) {
		CompletableFuture<MVPacket> receiver = requests.remove(packet.getRequestId());
		if (receiver != null)
			receiver.complete(packet);
	}
	
	@Override
	public void onPlayStart(ClientPlayNetworkHandler networkHandler) {
		status = Status.CLIENT_ONLY;
	}
	
	@Override
	public void onPlayStop() {
		status = Status.DISCONNECTED;
	}
	
	private void onProtocolVersionPacket(ProtocolVersionS2CPacket packet) {
		if (packet.getVersion() == NBTEditorServer.PROTOCOL_VERSION)
			status = Status.BOTH;
		else {
			status = Status.INCOMPATIBLE;
			if (ConfigScreen.isWarnIncompatibleProtocol()) {
				MVMisc.showToast(TextInst.translatable("nbteditor.incompatible_protocol.title"),
						TextInst.translatable("nbteditor.incompatible_protocol.desc"));
			}
		}
	}
	
	private void onContainerScreenPacket(ContainerScreenS2CPacket packet) {
		containerScreen = true;
	}
	
}
