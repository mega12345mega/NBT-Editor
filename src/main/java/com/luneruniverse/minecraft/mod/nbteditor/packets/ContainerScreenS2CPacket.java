package com.luneruniverse.minecraft.mod.nbteditor.packets;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class ContainerScreenS2CPacket implements FabricPacket {
	
	public static final PacketType<ContainerScreenS2CPacket> TYPE = PacketType.create(new Identifier("nbteditor", "container_screen"), ContainerScreenS2CPacket::new);
	
	public ContainerScreenS2CPacket() {}
	public ContainerScreenS2CPacket(PacketByteBuf payload) {}
	
	@Override
	public void write(PacketByteBuf payload) {}
	
	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
	
}
