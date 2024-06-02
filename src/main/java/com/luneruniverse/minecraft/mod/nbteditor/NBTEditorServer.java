package com.luneruniverse.minecraft.mod.nbteditor;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.luneruniverse.minecraft.mod.nbteditor.misc.BlockStateProperties;
import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.packets.GetBlockC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.GetEntityC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.GetLecternBlockC2SPacket;
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
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.LecternScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.math.Vec3d;

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
		ServerPlayNetworking.registerReceiver(network, GetLecternBlockC2SPacket.TYPE, this::onGetLecternBlockPacket);
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
				sendViewBlockPacket(packet.getRequestId(), block, sender);
				return;
			}
		}
		sender.sendPacket(new ViewBlockS2CPacket(packet.getRequestId(), packet.getWorld(), packet.getPos(), null, null, null));
	}
	private void onGetLecternBlockPacket(GetLecternBlockC2SPacket packet, ServerPlayerEntity player, PacketSender sender) {
		if (!player.hasPermissionLevel(2))
			return;
		if (player.currentScreenHandler instanceof LecternScreenHandler handler) {
			LecternBlockEntity lectern = MixinLink.getLectern(handler, player);
			if (lectern != null) {
				sendViewBlockPacket(packet.getRequestId(), lectern, sender);
				return;
			}
		}
		sender.sendPacket(new ViewBlockS2CPacket(packet.getRequestId(), null, null, null, null, null));
	}
	private void sendViewBlockPacket(int requestId, BlockEntity block, PacketSender sender) {
		sender.sendPacket(new ViewBlockS2CPacket(requestId, block.getWorld().getRegistryKey(), block.getPos(),
				MVRegistry.BLOCK.getId(block.getCachedState().getBlock()),
				new BlockStateProperties(block.getCachedState()), block.createNbt()));
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
		if (!MVRegistry.BLOCK.getId(state.getBlock()).equals(packet.getId())) {
			world.removeBlockEntity(packet.getPos());
			world.setBlockState(packet.getPos(),
					packet.getState().applyToSafely(MVRegistry.BLOCK.get(packet.getId()).getDefaultState()));
		} else {
			if (!new BlockStateProperties(state).equals(packet.getState()))
				world.setBlockState(packet.getPos(), packet.getState().applyTo(state));
			if (packet.isRecreate())
				world.removeBlockEntity(packet.getPos());
		}
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
		UUID newUUID = packet.getUUID();
		if (packet.getNbt().containsUuid("UUID")) {
			newUUID = packet.getNbt().getUuid("UUID");
			if (!packet.getUUID().equals(newUUID) && world.getEntity(newUUID) != null) {
				newUUID = packet.getUUID();
				packet.getNbt().putUuid("UUID", newUUID);
			}
		} else
			packet.getNbt().putUuid("UUID", packet.getUUID());
		if (packet.isRecreate() || !entity.getUuid().equals(newUUID) || !EntityType.getId(entity.getType()).equals(packet.getId())) {
			Entity vehicle = entity.getVehicle();
			Vec3d pos = entity.getPos();
			entity.streamPassengersAndSelf().forEach(passengerOrSelf -> {
				passengerOrSelf.stopRiding();
				passengerOrSelf.remove(RemovalReason.DISCARDED);
			});
			entity = MVRegistry.ENTITY_TYPE.get(packet.getId()).create(world);
			entity.setUuid(newUUID);
			entity.setPosition(pos);
			world.spawnEntity(entity);
			readEntityNbtWithPassengers(world, entity, packet.getNbt());
			if (vehicle != null)
				entity.startRiding(vehicle, true);
		} else {
			entity.getDataTracker().reset();
			readEntityNbtWithPassengers(world, entity, packet.getNbt());
		}
	}
	private void readEntityNbtWithPassengers(ServerWorld world, Entity entity, NbtCompound nbt) {
		entity.readNbt(nbt);
		
		Map<UUID, Entity> passengers = entity.getPassengerList().stream().collect(Collectors.toMap(Entity::getUuid, Function.identity()));
		NbtList passengersNbt = nbt.getList("Passengers", NbtElement.COMPOUND_TYPE);
		Set<UUID> passengerUUIDs = new HashSet<>();
		
		for (NbtElement passengerNbtElement : passengersNbt) {
			NbtCompound passengerNbt = (NbtCompound) passengerNbtElement;
			if (!passengerNbt.containsUuid("UUID"))
				passengerNbt.putUuid("UUID", UUID.randomUUID());
			UUID passengerUUID = passengerNbt.getUuid("UUID");
			if (!passengerUUIDs.add(passengerUUID)) {
				passengerUUID = UUID.randomUUID();
				passengerNbt.putUuid("UUID", passengerUUID);
			}
			Entity passenger = passengers.get(passengerUUID);
			
			Identifier passengerId = null;
			if (passengerNbt.contains("id", NbtElement.STRING_TYPE)) {
				try {
					passengerId = new Identifier(passengerNbt.getString("id"));
					if (!MVRegistry.ENTITY_TYPE.containsId(passengerId))
						passengerId = null;
				} catch (InvalidIdentifierException e) {}
			}
			if (passengerId != null && passenger != null && !EntityType.getId(passenger.getType()).equals(passengerId)) {
				passenger.streamPassengersAndSelf().forEach(passengerOrSelf -> {
					passengerOrSelf.stopRiding();
					passengerOrSelf.remove(RemovalReason.DISCARDED);
				});
				passenger = null;
			}
			
			if (passenger == null) {
				if (passengerId == null)
					continue;
				EntityType<?> passengerType = MVRegistry.ENTITY_TYPE.get(passengerId);
				if (world.getEntity(passengerUUID) != null) {
					passengerUUID = UUID.randomUUID();
					passengerNbt.putUuid("UUID", passengerUUID);
				}
				passenger = passengerType.create(world);
				passenger.setUuid(passengerUUID);
				passenger.startRiding(entity, true);
				world.spawnEntity(passenger);
			}
			
			readEntityNbtWithPassengers(world, passenger, passengerNbt);
		}
		
		passengers.keySet().removeAll(passengerUUIDs);
		for (Entity passenger : passengers.values()) {
			passenger.streamPassengersAndSelf().forEach(passengerOrSelf -> {
				passengerOrSelf.stopRiding();
				passengerOrSelf.remove(RemovalReason.DISCARDED);
			});
		}
	}
	
}
