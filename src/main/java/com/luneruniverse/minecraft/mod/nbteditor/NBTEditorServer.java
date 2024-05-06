package com.luneruniverse.minecraft.mod.nbteditor;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.packets.GetBlockC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.GetEntityC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.OpenEnderChestC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.ProtocolVersionS2CPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.SetBlockC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.SetCursorC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.SetEntityC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.SetSlotC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.ViewBlockS2CPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.ViewEntityS2CPacket;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class NBTEditorServer implements ServerPlayConnectionEvents.Init {
	
	public NBTEditorServer() {
		ServerPlayConnectionEvents.INIT.register(this);
	}
	
	@Override
	public void onPlayInit(ServerPlayNetworkHandler network, MinecraftServer server) {
		ServerPlayNetworking.send(network.getPlayer(), new ProtocolVersionS2CPacket(NBTEditorServerConn.PROTOCOL_VERSION));
		ServerPlayNetworking.registerReceiver(network, SetCursorC2SPacket.TYPE, this::onSetCursorPacket);
		ServerPlayNetworking.registerReceiver(network, SetSlotC2SPacket.TYPE, this::onSetSlotPacket);
		ServerPlayNetworking.registerReceiver(network, OpenEnderChestC2SPacket.TYPE, this::onOpenEnderChestPacket);
		ServerPlayNetworking.registerReceiver(network, GetBlockC2SPacket.TYPE, this::onGetBlockPacket);
		ServerPlayNetworking.registerReceiver(network, GetEntityC2SPacket.TYPE, this::onGetEntityPacket);
		ServerPlayNetworking.registerReceiver(network, SetBlockC2SPacket.TYPE, this::onSetBlockPacket);
		ServerPlayNetworking.registerReceiver(network, SetEntityC2SPacket.TYPE, this::onSetEntityPacket);
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
	
	private void onGetBlockPacket(GetBlockC2SPacket packet, ServerPlayerEntity player, PacketSender sender) {
		if (!player.hasPermissionLevel(2))
			return;
		ServerWorld world = player.getServer().getWorld(packet.getWorld());
		if (world != null) {
			BlockEntity block = world.getBlockEntity(packet.getPos());
			if (block != null) {
				sender.sendPacket(new ViewBlockS2CPacket(packet.getRequestId(),
						MVRegistry.BLOCK.getId(block.getCachedState().getBlock()), block.createNbt()));
				return;
			}
		}
		sender.sendPacket(new ViewBlockS2CPacket(packet.getRequestId(), null, null));
	}
	
	private void onGetEntityPacket(GetEntityC2SPacket packet, ServerPlayerEntity player, PacketSender sender) {
		if (!player.hasPermissionLevel(2))
			return;
		ServerWorld world = player.getServer().getWorld(packet.getWorld());
		if (world != null) {
			Entity entity = world.getEntity(packet.getUUID());
			if (entity != null && !(entity instanceof PlayerEntity)) {
				sender.sendPacket(new ViewEntityS2CPacket(packet.getRequestId(),
						EntityType.getId(entity.getType()), entity.writeNbt(new NbtCompound())));
				return;
			}
		}
		sender.sendPacket(new ViewEntityS2CPacket(packet.getRequestId(), null, null));
	}
	
	private void onSetBlockPacket(SetBlockC2SPacket packet, ServerPlayerEntity player, PacketSender sender) {
		if (!player.hasPermissionLevel(2))
			return;
		ServerWorld world = player.getServer().getWorld(packet.getWorld());
		if (world == null)
			return;
		BlockState state = world.getBlockState(packet.getPos());
		if (!MVRegistry.BLOCK.getId(state.getBlock()).equals(packet.getId()))
			world.setBlockState(packet.getPos(), MVRegistry.BLOCK.get(packet.getId()).getDefaultState());
		BlockEntity block = world.getBlockEntity(packet.getPos());
		if (block == null)
			return;
		block.readNbt(packet.getNbt());
		if (packet.isTriggerUpdate()) {
			block.markDirty();
			// Flags arg seems to be unused, and I don't know what it's supposed to be for this
			world.updateListeners(packet.getPos(), block.getCachedState(), block.getCachedState(), 0);
		}
	}
	
	private void onSetEntityPacket(SetEntityC2SPacket packet, ServerPlayerEntity player, PacketSender sender) {
		if (!player.hasPermissionLevel(2))
			return;
		ServerWorld world = player.getServer().getWorld(packet.getWorld());
		if (world == null)
			return;
		Entity entity = world.getEntity(packet.getUUID());
		if (entity == null)
			return;
		entity.getDataTracker().reset();
		entity.readNbt(packet.getNbt());
	}
	
}
