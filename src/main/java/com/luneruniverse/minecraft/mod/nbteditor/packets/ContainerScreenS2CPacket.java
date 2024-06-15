package com.luneruniverse.minecraft.mod.nbteditor.packets;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVPacket;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class ContainerScreenS2CPacket implements MVPacket {
	
	public static final Identifier ID = new Identifier("nbteditor", "container_screen");
	
	public ContainerScreenS2CPacket() {}
	public ContainerScreenS2CPacket(PacketByteBuf payload) {}
	
	@Override
	public void write(PacketByteBuf payload) {}
	
	@Override
	public Identifier id() {
		return ID;
	}
	
}
