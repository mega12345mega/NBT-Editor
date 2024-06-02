package com.luneruniverse.minecraft.mod.nbteditor.packets;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class GetLecternBlockC2SPacket implements FabricPacket {
	
	public static final PacketType<GetLecternBlockC2SPacket> TYPE = PacketType.create(new Identifier("nbteditor", "get_lectern_block"), GetLecternBlockC2SPacket::new);
	
	private final int requestId;
	
	public GetLecternBlockC2SPacket(int requestId) {
		this.requestId = requestId;
	}
	public GetLecternBlockC2SPacket(PacketByteBuf payload) {
		this.requestId = payload.readInt();
	}
	
	public int getRequestId() {
		return requestId;
	}
	
	@Override
	public void write(PacketByteBuf payload) {
		payload.writeInt(requestId);
	}
	
	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
	
}
