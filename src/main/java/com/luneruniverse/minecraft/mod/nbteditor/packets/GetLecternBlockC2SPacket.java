package com.luneruniverse.minecraft.mod.nbteditor.packets;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVPacket;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class GetLecternBlockC2SPacket implements MVPacket {
	
	public static final Identifier ID = new Identifier("nbteditor", "get_lectern_block");
	
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
	public Identifier id() {
		return ID;
	}
	
}
