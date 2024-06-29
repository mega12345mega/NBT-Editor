package com.luneruniverse.minecraft.mod.nbteditor.server;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorServerConn;
import com.luneruniverse.minecraft.mod.nbteditor.misc.BlockStateProperties;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.NBTManagers;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVServerNetworking;
import com.luneruniverse.minecraft.mod.nbteditor.packets.GetBlockC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.GetEntityC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.GetLecternBlockC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.OpenEnderChestC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.ProtocolVersionS2CPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.SetBlockC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.SetCursorC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.SetEntityC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.SetSlotC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.SummonEntityC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.ViewBlockS2CPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.ViewEntityS2CPacket;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.LecternScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.math.Vec3d;

public class NBTEditorServer implements MVServerNetworking.PlayNetworkStateEvents.Start {
	
	public static boolean IS_DEDICATED = true;
	
	public NBTEditorServer() {
		MVServerNetworking.registerListener(SetCursorC2SPacket.ID, this::onSetCursorPacket);
		MVServerNetworking.registerListener(SetSlotC2SPacket.ID, this::onSetSlotPacket);
		MVServerNetworking.registerListener(OpenEnderChestC2SPacket.ID, this::onOpenEnderChestPacket);
		MVServerNetworking.registerListener(GetBlockC2SPacket.ID, this::onGetBlockPacket);
		MVServerNetworking.registerListener(GetLecternBlockC2SPacket.ID, this::onGetLecternBlockPacket);
		MVServerNetworking.registerListener(GetEntityC2SPacket.ID, this::onGetEntityPacket);
		MVServerNetworking.registerListener(SetBlockC2SPacket.ID, this::onSetBlockPacket);
		MVServerNetworking.registerListener(SetEntityC2SPacket.ID, this::onSetEntityPacket);
		MVServerNetworking.registerListener(SummonEntityC2SPacket.ID, this::onSummonEntityPacket);
		
		MVServerNetworking.PlayNetworkStateEvents.Start.EVENT.register(this);
	}
	
	@Override
	public void onPlayStart(ServerPlayerEntity player) {
		MVServerNetworking.send(player, new ProtocolVersionS2CPacket(NBTEditorServerConn.PROTOCOL_VERSION));
	}
	
	private void onSetCursorPacket(SetCursorC2SPacket packet, ServerPlayerEntity player) {
		if (!player.hasPermissionLevel(2))
			return;
		
		player.currentScreenHandler.setCursorStack(packet.getItem());
	}
	
	private void onSetSlotPacket(SetSlotC2SPacket packet, ServerPlayerEntity player) {
		if (!player.hasPermissionLevel(2))
			return;
		if (player.currentScreenHandler == player.playerScreenHandler)
			return;
		
		Slot slot = player.currentScreenHandler.getSlot(packet.getSlot());
		if (slot.inventory == player.getInventory())
			return;
		
		slot.setStackNoCallbacks(packet.getItem());
	}
	
	private void onOpenEnderChestPacket(OpenEnderChestC2SPacket packet, ServerPlayerEntity player) {
		if (!player.hasPermissionLevel(2))
			return;
		
		player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, inventory, player2) ->
				GenericContainerScreenHandler.createGeneric9x3(syncId, inventory, player.getEnderChestInventory()),
				TextInst.translatable("container.enderchest")));
	}
	
	private void onGetBlockPacket(GetBlockC2SPacket packet, ServerPlayerEntity player) {
		if (!player.hasPermissionLevel(2))
			return;
		
		ServerWorld world = player.getServer().getWorld(packet.getWorld());
		if (world != null) {
			BlockEntity blockEntity = world.getBlockEntity(packet.getPos());
			if (blockEntity != null) {
				sendViewBlockPacket(packet.getRequestId(), blockEntity, player);
				return;
			}
		}
		
		MVServerNetworking.send(player, new ViewBlockS2CPacket(packet.getRequestId(), packet.getWorld(), packet.getPos(), null, null, null));
	}
	private void onGetLecternBlockPacket(GetLecternBlockC2SPacket packet, ServerPlayerEntity player) {
		if (!player.hasPermissionLevel(2))
			return;
		
		if (player.currentScreenHandler instanceof LecternScreenHandler handler) {
			// Get the LecternBlockEntity from the inventory's synthetic reference to its enclosing class
			Inventory inv = handler.inventory;
			LecternBlockEntity lectern = Reflection.getField(inv.getClass(), "field_17391", "Lnet/minecraft/class_3722;").get(inv);
			if (lectern != null) {
				sendViewBlockPacket(packet.getRequestId(), lectern, player);
				return;
			}
		}
		
		MVServerNetworking.send(player, new ViewBlockS2CPacket(packet.getRequestId(), null, null, null, null, null));
	}
	private void sendViewBlockPacket(int requestId, BlockEntity blockEntity, ServerPlayerEntity player) {
		MVServerNetworking.send(player,
				new ViewBlockS2CPacket(requestId, blockEntity.getWorld().getRegistryKey(), blockEntity.getPos(),
						MVRegistry.BLOCK.getId(blockEntity.getCachedState().getBlock()),
						new BlockStateProperties(blockEntity.getCachedState()),
						NBTManagers.BLOCK_ENTITY.getNbt(blockEntity)));
	}
	
	private void onGetEntityPacket(GetEntityC2SPacket packet, ServerPlayerEntity player) {
		if (!player.hasPermissionLevel(2))
			return;
		
		ServerWorld world = player.getServer().getWorld(packet.getWorld());
		if (world != null) {
			Entity entity = world.getEntity(packet.getUUID());
			if (entity != null && !(entity instanceof PlayerEntity)) {
				MVServerNetworking.send(player,
						new ViewEntityS2CPacket(packet.getRequestId(),
								entity.getEntityWorld().getRegistryKey(), entity.getUuid(),
								EntityType.getId(entity.getType()), entity.writeNbt(new NbtCompound())));
				return;
			}
		}
		
		MVServerNetworking.send(player, new ViewEntityS2CPacket(packet.getRequestId(), packet.getWorld(), packet.getUUID(), null, null));
	}
	
	private void onSetBlockPacket(SetBlockC2SPacket packet, ServerPlayerEntity player) {
		if (!player.hasPermissionLevel(2))
			return;
		
		ServerWorld world = player.getServer().getWorld(packet.getWorld());
		if (world == null)
			return;
		
		Block block = MVRegistry.BLOCK.get(packet.getId());
		BlockState state = world.getBlockState(packet.getPos());
		if (state.getBlock() != block) {
			world.removeBlockEntity(packet.getPos());
			world.setBlockState(packet.getPos(),
					packet.getState().applyToSafely(block.getDefaultState()));
		} else {
			if (!new BlockStateProperties(state).equals(packet.getState()))
				world.setBlockState(packet.getPos(), packet.getState().applyTo(state));
			if (packet.isRecreate())
				world.removeBlockEntity(packet.getPos());
		}
		
		BlockEntity blockEntity = world.getBlockEntity(packet.getPos());
		if (blockEntity == null)
			return;
		
		NBTManagers.BLOCK_ENTITY.setNbt(blockEntity, packet.getNbt());
		
		if (packet.isTriggerUpdate()) {
			blockEntity.markDirty();
			// Flags arg seems to be unused, and I don't know what it's supposed to be for this
			world.updateListeners(packet.getPos(), blockEntity.getCachedState(), blockEntity.getCachedState(), 0);
		}
	}
	
	private void onSetEntityPacket(SetEntityC2SPacket packet, ServerPlayerEntity player) {
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
		
		EntityType<?> entityType = MVRegistry.ENTITY_TYPE.get(packet.getId());
		
		if (packet.isRecreate() || !entity.getUuid().equals(newUUID) || entity.getType() != entityType) {
			Entity vehicle = entity.getVehicle();
			Vec3d pos = entity.getPos();
			entity.streamPassengersAndSelf().forEach(passengerOrSelf -> {
				passengerOrSelf.stopRiding();
				passengerOrSelf.remove(RemovalReason.DISCARDED);
			});
			entity = entityType.create(world);
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
	
	private void onSummonEntityPacket(SummonEntityC2SPacket packet, ServerPlayerEntity player) {
		if (!player.hasPermissionLevel(2))
			return;
		
		ServerWorld world = player.getServer().getWorld(packet.getWorld());
		if (world == null) {
			MVServerNetworking.send(player, new ViewEntityS2CPacket(packet.getRequestId(), null, null, null, null));
			return;
		}
		
		UUID uuid = UUID.randomUUID();
		if (packet.getNbt().containsUuid("UUID")) {
			UUID nbtUUID = packet.getNbt().getUuid("UUID");
			if (world.getEntity(nbtUUID) == null)
				uuid = nbtUUID;
			else
				packet.getNbt().putUuid("UUID", uuid);
		}
		
		Entity entity = MVRegistry.ENTITY_TYPE.get(packet.getId()).create(world);
		entity.setUuid(uuid);
		entity.setPosition(packet.getPos());
		packet.getNbt().put("Pos", Stream.of(packet.getPos().x, packet.getPos().y, packet.getPos().z)
				.map(NbtDouble::of).collect(NbtList::new, NbtList::add, NbtList::addAll));
		world.spawnEntity(entity);
		readEntityNbtWithPassengers(world, entity, packet.getNbt());
		
		MVServerNetworking.send(player,
				new ViewEntityS2CPacket(packet.getRequestId(), entity.getEntityWorld().getRegistryKey(), entity.getUuid(),
						EntityType.getId(entity.getType()), entity.writeNbt(new NbtCompound())));
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
