package com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class MVNetworking {
	
	private static final Map<Identifier, Function<PacketByteBuf, MVPacket>> constructors = new HashMap<>();
	
	public static void registerPacket(Identifier id, Function<PacketByteBuf, MVPacket> constructor) {
		constructors.put(id, constructor);
	}
	
	public static MVPacket readPacket(Identifier id, PacketByteBuf payload) {
		Function<PacketByteBuf, MVPacket> constructor = constructors.get(id);
		if (constructor == null)
			return null;
		return constructor.apply(payload);
	}
	
}
