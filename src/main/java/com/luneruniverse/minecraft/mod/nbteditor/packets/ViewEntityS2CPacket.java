package com.luneruniverse.minecraft.mod.nbteditor.packets;

import java.util.UUID;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistryKeys;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class ViewEntityS2CPacket implements ResponsePacket {
	
	public static final Identifier ID = new Identifier("nbteditor", "view_entity");
	
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
		this.requestId = payload.readVarInt();
		if (payload.readBoolean()) {
			this.world = payload.readRegistryKey(MVRegistryKeys.WORLD);
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
		payload.writeVarInt(requestId);
		if (world == null) {
			payload.writeBoolean(false);
		} else {
			payload.writeBoolean(true);
			payload.writeRegistryKey(world);
			payload.writeUuid(uuid);
		}
		if (id == null) {
			payload.writeBoolean(false);
		} else {
			payload.writeBoolean(true);
			payload.writeIdentifier(id);
			payload.writeNbtCompound(nbt);
		}
	}
	
	@Override
	public Identifier getPacketId() {
		return ID;
	}
	
}
