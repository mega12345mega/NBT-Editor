package com.luneruniverse.minecraft.mod.nbteditor.packets;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVPacket;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class ProtocolVersionS2CPacket implements MVPacket {
	
	public static final Identifier ID = new Identifier("nbteditor", "protocol_version");
	
	private final int version;
	
	public ProtocolVersionS2CPacket(int version) {
		this.version = version;
	}
	public ProtocolVersionS2CPacket(PacketByteBuf payload) {
		this.version = payload.readVarInt();
	}
	
	public int getVersion() {
		return version;
	}
	
	@Override
	public void write(PacketByteBuf payload) {
		payload.writeVarInt(version);
	}
	
	@Override
	public Identifier getPacketId() {
		return ID;
	}
	
}
