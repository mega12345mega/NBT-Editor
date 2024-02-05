package com.luneruniverse.minecraft.mod.nbteditor.packets;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class SetCursorC2SPacket implements FabricPacket {
	
	public static final PacketType<SetCursorC2SPacket> TYPE = PacketType.create(new Identifier("nbteditor", "set_cursor"), SetCursorC2SPacket::new);
	
	private final ItemStack item;
	
	public SetCursorC2SPacket(ItemStack item) {
		this.item = item;
	}
	public SetCursorC2SPacket(PacketByteBuf payload) {
		this.item = payload.readItemStack();
	}
	
	public ItemStack getItem() {
		return item;
	}
	
	@Override
	public void write(PacketByteBuf payload) {
		payload.writeItemStack(item);
	}
	
	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
	
}
