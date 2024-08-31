package com.luneruniverse.minecraft.mod.nbteditor.packets;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IdentifierInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVPacket;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class OpenEnderChestC2SPacket implements MVPacket {
	
	public static final Identifier ID = IdentifierInst.of("nbteditor", "open_ender_chest");
	
	public OpenEnderChestC2SPacket() {}
	public OpenEnderChestC2SPacket(PacketByteBuf payload) {}
	
	@Override
	public void write(PacketByteBuf payload) {}
	
	@Override
	public Identifier getPacketId() {
		return ID;
	}
	
}
