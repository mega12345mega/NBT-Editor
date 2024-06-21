package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public interface MVPacketByteBufParent {
	
	public default PacketByteBuf writeBoolean(boolean value) {
		throw new RuntimeException("Missing implementation for MVPacketByteBufParent#writeBoolean");
	}
	
	public default Identifier readIdentifier() {
		throw new RuntimeException("Missing implementation for MVPacketByteBufParent#readIdentifier");
	}
	public default PacketByteBuf writeIdentifier(Identifier id) {
		throw new RuntimeException("Missing implementation for MVPacketByteBufParent#writeIdentifier");
	}
	
	public default <T> RegistryKey<T> readRegistryKey(RegistryKey<? extends Registry<T>> registryRef) {
		throw new RuntimeException("Missing implementation for MVPacketByteBufParent#readRegistryKey");
	}
	public default void writeRegistryKey(RegistryKey<?> key) {
		throw new RuntimeException("Missing implementation for MVPacketByteBufParent#writeRegistryKey");
	}
	
}
