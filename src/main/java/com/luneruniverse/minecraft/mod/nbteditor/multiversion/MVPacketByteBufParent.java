package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public interface MVPacketByteBufParent {
	
	public default PacketByteBuf writeBoolean(boolean value) {
		throw new RuntimeException("Missing implementation for MVPacketByteBufParent#writeBoolean");
	}
	
	public default PacketByteBuf writeDouble(double value) {
		throw new RuntimeException("Missing implementation for MVPacketByteBufParent#writeDouble");
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
	
	public default PacketByteBuf writeNbtCompound(NbtCompound element) {
		throw new RuntimeException("Missing implementation for MVPacketByteBufParent#writeNbtCompound");
	}
	
	public default Vec3d readVec3d() {
		throw new RuntimeException("Missing implementation for MVPacketByteBufParent#readVec3d");
	}
	public default void writeVec3d(Vec3d vector) {
		throw new RuntimeException("Missing implementation for MVPacketByteBufParent#writeVec3d");
	}
	
	public default ItemStack readItemStack() {
		throw new RuntimeException("Missing implementation for MVPacketByteBufParent#readItemStack");
	}
	public default PacketByteBuf writeItemStack(ItemStack item) {
		throw new RuntimeException("Missing implementation for MVPacketByteBufParent#writeItemStack");
	}
	
}
