package com.luneruniverse.minecraft.mod.nbteditor.packets;

public interface ClickSlotC2SPacketParent {
	public default boolean isNoArmorRestriction() {
		throw new RuntimeException("Missing implementation for ClickSlotC2SPacketParent#isNoArmorRestriction");
	}
}
