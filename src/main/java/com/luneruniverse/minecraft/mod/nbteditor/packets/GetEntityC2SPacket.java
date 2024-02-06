package com.luneruniverse.minecraft.mod.nbteditor.packets;

import java.util.UUID;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class GetEntityC2SPacket implements FabricPacket {
	
	public static final PacketType<GetEntityC2SPacket> TYPE = PacketType.create(new Identifier("nbteditor", "get_entity"), GetEntityC2SPacket::new);
	
	private final int requestId;
	private final RegistryKey<World> world;
	private final UUID uuid;
	
	public GetEntityC2SPacket(int requestId, RegistryKey<World> world, UUID uuid) {
		this.requestId = requestId;
		this.world = world;
		this.uuid = uuid;
	}
	public GetEntityC2SPacket(PacketByteBuf payload) {
		this.requestId = payload.readInt();
		this.world = payload.readRegistryKey(payload.readRegistryRefKey());
		this.uuid = payload.readUuid();
	}
	
	public int getRequestId() {
		return requestId;
	}
	public RegistryKey<World> getWorld() {
		return world;
	}
	public UUID getUUID() {
		return uuid;
	}
	
	@Override
	public void write(PacketByteBuf payload) {
		payload.writeInt(requestId);
		payload.writeIdentifier(world.getRegistry());
		payload.writeRegistryKey(world);
		payload.writeUuid(uuid);
	}
	
	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
	
}
