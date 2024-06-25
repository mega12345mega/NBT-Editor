package com.luneruniverse.minecraft.mod.nbteditor.server;

import java.lang.invoke.MethodType;
import java.util.function.Supplier;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.vehicle.StorageMinecartEntity;
import net.minecraft.entity.vehicle.VehicleInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;

public class ServerMVMisc {
	
	private static final Supplier<Reflection.MethodInvoker> EntityTrackingListener_sendPacket =
			Reflection.getOptionalMethod(() -> Reflection.getClass("net.minecraft.class_5629"), () -> "method_14364", () -> MethodType.methodType(void.class, Packet.class));
	public static void sendS2CPacket(ServerPlayerEntity player, Packet<?> packet) {
		Version.newSwitch()
				.range("1.20.2", null, () -> player.networkHandler.sendPacket(packet))
				.range(null, "1.20.1", () -> EntityTrackingListener_sendPacket.get().invoke(player.networkHandler, packet))
				.run();
	}
	
	public static boolean isInstanceOfVehicleInventory(NamedScreenHandlerFactory factory) {
		return Version.<Boolean>newSwitch()
				.range("1.19.0", null, () -> factory instanceof VehicleInventory)
				.range(null, "1.18.2", () -> factory instanceof StorageMinecartEntity)
				.get();
	}
	
	private static final Supplier<Reflection.MethodInvoker> BlockEntity_writeNbt =
			Reflection.getOptionalMethod(BlockEntity.class, "method_11007", MethodType.methodType(NbtCompound.class, NbtCompound.class));
	public static NbtCompound createNbt(BlockEntity blockEntity) {
		return Version.<NbtCompound>newSwitch()
				.range("1.18.0", null, () -> blockEntity.createNbt())
				.range(null, "1.17.1", () -> {
					ServerMixinLink.BLOCK_ENTITY_WRITE_NBT_WITHOUT_IDENTIFYING_DATA.add(Thread.currentThread());
					return BlockEntity_writeNbt.get().invoke(blockEntity, new NbtCompound());
				})
				.get();
	}
	
}
