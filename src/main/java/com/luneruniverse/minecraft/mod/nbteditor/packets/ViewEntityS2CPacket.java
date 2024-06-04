package com.luneruniverse.minecraft.mod.nbteditor.packets;

import java.util.UUID;

import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class ViewEntityS2CPacket implements ResponsePacket {
	
	public static final PacketType<ViewEntityS2CPacket> TYPE = PacketType.create(new Identifier("nbteditor", "view_entity"), ViewEntityS2CPacket::new);
	
	private final int requestId;
	private final RegistryKey<World> world;
	private final UUID uuid;
	private final Identifier id;
	private final NbtCompound nbt;
	
	public ViewEntityS2CPacket(int requestId, RegistryKey<World> world, UUID uuid, Identifier id, NbtCompound nbt) {
		if ((world == null) != (uuid == null))
			throw new IllegalArgumentException("world and uuid have to be null together!");
		if ((id == null) != (nbt == null))
			throw new IllegalArgumentException("id and nbt have to be null together!");
		
		this.requestId = requestId;
		this.world = world;
		this.uuid = uuid;
		this.id = id;
		this.nbt = nbt;
	}
	public ViewEntityS2CPacket(PacketByteBuf payload) {
		this.requestId = payload.readInt();
		if (payload.readBoolean()) {
			this.world = payload.readRegistryKey(payload.<World>readRegistryRefKey());
			this.uuid = payload.readUuid();
		} else {
			this.world = null;
			this.uuid = null;
		}
		if (payload.readBoolean()) {
			this.id = payload.readIdentifier();
			this.nbt = payload.readNbt();
		} else {
			this.id = null;
			this.nbt = null;
		}
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
	public boolean foundEntity() {
		return id != null;
	}
	public Identifier getId() {
		return id;
	}
	public NbtCompound getNbt() {
		return nbt;
	}
	
	@Override
	public void write(PacketByteBuf payload) {
		payload.writeInt(requestId);
		if (world == null) {
			payload.writeBoolean(false);
		} else {
			payload.writeBoolean(true);
			payload.writeIdentifier(world.getRegistry());
			payload.writeRegistryKey(world);
			payload.writeUuid(uuid);
		}
		if (id == null) {
			payload.writeBoolean(false);
		} else {
			payload.writeBoolean(true);
			payload.writeIdentifier(id);
			payload.writeNbt(nbt);
		}
	}
	
	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
	
}
