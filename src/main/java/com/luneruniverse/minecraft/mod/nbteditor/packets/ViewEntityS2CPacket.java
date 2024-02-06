package com.luneruniverse.minecraft.mod.nbteditor.packets;

import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class ViewEntityS2CPacket implements ResponsePacket {
	
	public static final PacketType<ViewEntityS2CPacket> TYPE = PacketType.create(new Identifier("nbteditor", "view_entity"), ViewEntityS2CPacket::new);
	
	private final int requestId;
	private final Identifier id;
	private final NbtCompound nbt;
	
	public ViewEntityS2CPacket(int requestId, Identifier id, NbtCompound nbt) {
		if ((id == null) != (nbt == null))
			throw new IllegalArgumentException("id and nbt have to be null together!");
		
		this.requestId = requestId;
		this.id = id;
		this.nbt = nbt;
	}
	public ViewEntityS2CPacket(PacketByteBuf payload) {
		this.requestId = payload.readInt();
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
