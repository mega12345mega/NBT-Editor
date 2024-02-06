package com.luneruniverse.minecraft.mod.nbteditor.packets;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;

public interface ResponsePacket extends FabricPacket {
	public int getRequestId();
}
