package com.luneruniverse.minecraft.mod.nbteditor.multiversion.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVPacketByteBufParent;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

@Mixin(PacketByteBuf.class)
public abstract class PacketByteBufMixin implements MVPacketByteBufParent {
	
	@Shadow
	public abstract String readString();
	@Shadow
	public abstract PacketByteBuf writeString(String str);
	
	@Override
	public PacketByteBuf writeBoolean(boolean value) {
		((ByteBuf) (Object) this).writeBoolean(value);
		return (PacketByteBuf) (Object) this;
	}
	
	@Override
	public Identifier readIdentifier() {
		return new Identifier(readString());
	}
	@Override
	public PacketByteBuf writeIdentifier(Identifier id) {
		return writeString(id.toString());
	}
	
	@Override
	public <T> RegistryKey<T> readRegistryKey(RegistryKey<? extends Registry<T>> registryRef) {
		return RegistryKey.of(registryRef, readIdentifier());
	}
	@Override
	public void writeRegistryKey(RegistryKey<?> key) {
		writeIdentifier(key.getValue());
	}
	
}
