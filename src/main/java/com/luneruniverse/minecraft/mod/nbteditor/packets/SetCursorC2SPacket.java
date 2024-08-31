package com.luneruniverse.minecraft.mod.nbteditor.packets;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IdentifierInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVPacket;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class SetCursorC2SPacket implements MVPacket {
	
	public static final Identifier ID = IdentifierInst.of("nbteditor", "set_cursor");
	
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
	public Identifier getPacketId() {
		return ID;
	}
	
}
