package com.luneruniverse.minecraft.mod.nbteditor.multiversion.mixin;

import java.lang.invoke.MethodType;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.DynamicRegistryManagerHolder;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVPacketByteBufParent;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

@Mixin(PacketByteBuf.class)
public abstract class PacketByteBufMixin implements MVPacketByteBufParent {
	
	@Shadow
	private ByteBuf parent;
	@Shadow
	public abstract String readString();
	@Shadow
	public abstract PacketByteBuf writeString(String str);
	@Shadow
	public abstract double readDouble();
	
	@Override
	public PacketByteBuf writeBoolean(boolean value) {
		parent.writeBoolean(value);
		return (PacketByteBuf) (Object) this;
	}
	
	@Override
	public PacketByteBuf writeDouble(double value) {
		parent.writeDouble(value);
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
	
	private static final Supplier<Reflection.MethodInvoker> PacketByteBuf_writeNbt =
			Reflection.getOptionalMethod(PacketByteBuf.class, "method_10794", MethodType.methodType(PacketByteBuf.class, NbtCompound.class));
	@Override
	public PacketByteBuf writeNbtCompound(NbtCompound element) {
		return Version.<PacketByteBuf>newSwitch()
				.range("1.20.2", null, () -> ((PacketByteBuf) (Object) this).writeNbt(element))
				.range(null, "1.20.1", () -> PacketByteBuf_writeNbt.get().invoke(this, (NbtCompound) element))
				.get();
	}
	
	@Override
	public Vec3d readVec3d() {
		return new Vec3d(readDouble(), readDouble(), readDouble());
	}
	@Override
	public void writeVec3d(Vec3d vector) {
		writeDouble(vector.getX());
		writeDouble(vector.getY());
		writeDouble(vector.getZ());
	}
	
	private static final Supplier<Reflection.MethodInvoker> PacketByteBuf_readItemStack =
			Reflection.getOptionalMethod(PacketByteBuf.class, "method_10819", MethodType.methodType(ItemStack.class));
	@Override
	public ItemStack readItemStack() {
		return Version.<ItemStack>newSwitch()
				.range("1.20.5", null, () -> MVMisc.packetCodecDecode(ItemStack.OPTIONAL_PACKET_CODEC, createRegistryByteBuf()))
				.range(null, "1.20.4", () -> PacketByteBuf_readItemStack.get().invoke(this))
				.get();
	}
	private static final Supplier<Reflection.MethodInvoker> PacketByteBuf_writeItemStack =
			Reflection.getOptionalMethod(PacketByteBuf.class, "method_10793", MethodType.methodType(PacketByteBuf.class, ItemStack.class));
	@Override
	public PacketByteBuf writeItemStack(ItemStack item) {
		Version.newSwitch()
				.range("1.20.5", null, () -> MVMisc.packetCodecEncode(ItemStack.OPTIONAL_PACKET_CODEC, createRegistryByteBuf(), item))
				.range(null, "1.20.4", () -> PacketByteBuf_writeItemStack.get().invoke(this, item))
				.run();
		return (PacketByteBuf) (Object) this;
	}
	
	private Object createRegistryByteBuf() {
		return Reflection.newInstance("net.minecraft.class_9129",
				new Class<?>[] {ByteBuf.class, DynamicRegistryManager.class},
				parent, DynamicRegistryManagerHolder.get());
	}
	
}
