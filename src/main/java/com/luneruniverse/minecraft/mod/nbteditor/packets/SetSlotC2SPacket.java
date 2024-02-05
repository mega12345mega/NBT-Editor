package com.luneruniverse.minecraft.mod.nbteditor.packets;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class SetSlotC2SPacket implements FabricPacket {
	
	public static final PacketType<SetSlotC2SPacket> TYPE = PacketType.create(new Identifier("nbteditor", "set_slot"), SetSlotC2SPacket::new);
	
	private final int slot;
	private final ItemStack item;
	
	public SetSlotC2SPacket(int slot, ItemStack item) {
		this.slot = slot;
		this.item = item;
	}
	public SetSlotC2SPacket(PacketByteBuf payload) {
		this.slot = payload.readInt();
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
		payload.writeInt(slot);
		payload.writeItemStack(item);
	}
	
	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
	
}
