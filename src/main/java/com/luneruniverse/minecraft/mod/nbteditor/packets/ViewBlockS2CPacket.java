package com.luneruniverse.minecraft.mod.nbteditor.packets;

import com.luneruniverse.minecraft.mod.nbteditor.misc.BlockStateProperties;

import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class ViewBlockS2CPacket implements ResponsePacket {
	
	public static final PacketType<ViewBlockS2CPacket> TYPE = PacketType.create(new Identifier("nbteditor", "view_block"), ViewBlockS2CPacket::new);
	
	private final int requestId;
	private final Identifier id;
	private final BlockStateProperties state;
	private final NbtCompound nbt;
	
	public ViewBlockS2CPacket(int requestId, Identifier id, BlockStateProperties state, NbtCompound nbt) {
		if ((id == null) != (state == null) || (id == null) != (nbt == null))
			throw new IllegalArgumentException("id, state, and nbt have to be null together!");
		
		this.requestId = requestId;
		this.id = id;
		this.state = state;
		this.nbt = nbt;
	}
	public ViewBlockS2CPacket(PacketByteBuf payload) {
		this.requestId = payload.readInt();
		if (payload.readBoolean()) {
			this.id = payload.readIdentifier();
			this.state = new BlockStateProperties(payload);
			this.nbt = payload.readNbt();
		} else {
			this.id = null;
			this.state = null;
			this.nbt = null;
		}
	}
	
	public int getRequestId() {
		return requestId;
	}
	public boolean foundBlock() {
		return id != null;
	}
	public Identifier getId() {
		return id;
	}
	public BlockStateProperties getState() {
		return state;
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
			state.writeToPayload(payload);
			payload.writeNbt(nbt);
		}
	}
	
	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
	
}
