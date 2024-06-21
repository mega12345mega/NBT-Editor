package com.luneruniverse.minecraft.mod.nbteditor.packets;

import java.util.UUID;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistryKeys;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVPacket;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class GetEntityC2SPacket implements MVPacket {
	
	public static final Identifier ID = new Identifier("nbteditor", "get_entity");
	
	private final int requestId;
	private final RegistryKey<World> world;
	private final UUID uuid;
	
	public GetEntityC2SPacket(int requestId, RegistryKey<World> world, UUID uuid) {
		this.requestId = requestId;
		this.world = world;
		this.uuid = uuid;
	}
	public GetEntityC2SPacket(PacketByteBuf payload) {
		this.requestId = payload.readVarInt();
		this.world = payload.readRegistryKey(MVRegistryKeys.WORLD);
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
		payload.writeVarInt(requestId);
		payload.writeRegistryKey(world);
		payload.writeUuid(uuid);
	}
	
	@Override
	public Identifier getPacketId() {
		return ID;
	}
	
}
