package com.luneruniverse.minecraft.mod.nbteditor.packets;

public interface ClickSlotC2SPacketParent {
	public default boolean isNoSlotRestrictions() {
		throw new RuntimeException("Missing implementation for ClickSlotC2SPacketParent#isNoSlotRestrictions");
	}
}
