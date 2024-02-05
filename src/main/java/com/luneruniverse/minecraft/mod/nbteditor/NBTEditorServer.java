package com.luneruniverse.minecraft.mod.nbteditor;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.packets.OpenEnderChestC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.SetCursorC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.SetSlotC2SPacket;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class NBTEditorServer implements ServerPlayConnectionEvents.Init {
	
	public NBTEditorServer() {
		ServerPlayConnectionEvents.INIT.register(this);
	}
	
	@Override
	public void onPlayInit(ServerPlayNetworkHandler network, MinecraftServer server) {
		ServerPlayNetworking.registerReceiver(network, SetCursorC2SPacket.TYPE, this::onSetCursorPacket);
		ServerPlayNetworking.registerReceiver(network, SetSlotC2SPacket.TYPE, this::onSetSlotPacket);
		ServerPlayNetworking.registerReceiver(network, OpenEnderChestC2SPacket.TYPE, this::onOpenEnderChestPacket);
	}
	
	private void onSetCursorPacket(SetCursorC2SPacket packet, ServerPlayerEntity player, PacketSender sender) {
		if (!player.hasPermissionLevel(2))
			return;
		player.currentScreenHandler.setCursorStack(packet.getItem());
	}
	
	private void onSetSlotPacket(SetSlotC2SPacket packet, ServerPlayerEntity player, PacketSender sender) {
		if (!player.hasPermissionLevel(2))
			return;
		if (player.currentScreenHandler == player.playerScreenHandler)
			return;
		Slot slot = player.currentScreenHandler.getSlot(packet.getSlot());
		if (slot.inventory == player.getInventory())
			return;
		slot.setStack(packet.getItem());
	}
	
	private void onOpenEnderChestPacket(OpenEnderChestC2SPacket packet, ServerPlayerEntity player, PacketSender sender) {
		if (!player.hasPermissionLevel(2))
			return;
		player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, inventory, player2) ->
				GenericContainerScreenHandler.createGeneric9x3(syncId, inventory, player.getEnderChestInventory()), TextInst.translatable("container.enderchest")));
	}
	
}
