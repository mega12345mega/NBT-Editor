package com.luneruniverse.minecraft.mod.nbteditor.packets;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class ProtocolVersionS2CPacket implements FabricPacket {
	
	public static final PacketType<ProtocolVersionS2CPacket> TYPE = PacketType.create(new Identifier("nbteditor", "protocol_version"), ProtocolVersionS2CPacket::new);
	
	private final int version;
	
	public ProtocolVersionS2CPacket(int version) {
		this.version = version;
	}
	public ProtocolVersionS2CPacket(PacketByteBuf payload) {
		this.version = payload.readInt();
	}
	
	public int getVersion() {
		return version;
	}
	
	@Override
	public void write(PacketByteBuf payload) {
		payload.writeInt(version);
	}
	
	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
	
}
