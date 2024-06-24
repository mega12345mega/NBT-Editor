package com.luneruniverse.minecraft.mod.nbteditor.server;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.listener.PacketListener;

public class ClientLink {
	
	public static boolean isInstanceOfClientPlayNetworkHandler(PacketListener listener) {
		return listener instanceof ClientPlayNetworkHandler;
	}
	
}
