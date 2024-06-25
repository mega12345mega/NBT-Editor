package com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public interface MVPacket {
	public void write(PacketByteBuf payload);
	public Identifier getPacketId();
}
