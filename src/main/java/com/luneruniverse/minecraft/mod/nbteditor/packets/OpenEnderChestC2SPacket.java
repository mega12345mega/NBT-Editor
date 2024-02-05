package com.luneruniverse.minecraft.mod.nbteditor.packets;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class OpenEnderChestC2SPacket implements FabricPacket {
	
	public static final PacketType<OpenEnderChestC2SPacket> TYPE = PacketType.create(new Identifier("nbteditor", "open_ender_chest"), OpenEnderChestC2SPacket::new);
	
	public OpenEnderChestC2SPacket() {}
	public OpenEnderChestC2SPacket(PacketByteBuf payload) {}
	
	@Override
	public void write(PacketByteBuf payload) {}
	
	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
	
}
