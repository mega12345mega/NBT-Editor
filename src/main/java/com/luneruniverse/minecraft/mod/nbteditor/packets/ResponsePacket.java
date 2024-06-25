package com.luneruniverse.minecraft.mod.nbteditor.packets;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVPacket;

public interface ResponsePacket extends MVPacket {
	public int getRequestId();
}
