package com.luneruniverse.minecraft.mod.nbteditor.packets;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVPacket;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class SetSlotC2SPacket implements MVPacket {
	
	public static final Identifier ID = new Identifier("nbteditor", "set_slot");
	
	private final int slot;
	private final ItemStack item;
	
	public SetSlotC2SPacket(int slot, ItemStack item) {
		this.slot = slot;
		this.item = item;
	}
	public SetSlotC2SPacket(PacketByteBuf payload) {
		this.slot = payload.readVarInt();
		this.item = payload.readItemStack();
	}
	
	public int getSlot() {
		return slot;
	}
	public ItemStack getItem() {
		return item;
	}
	
	@Override
	public void write(PacketByteBuf payload) {
		payload.writeVarInt(slot);
		payload.writeItemStack(item);
	}
	
	@Override
	public Identifier getPacketId() {
		return ID;
	}
	
}
