package com.luneruniverse.minecraft.mod.nbteditor.server;

import java.lang.invoke.MethodType;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Supplier;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.StorageMinecartEntity;
import net.minecraft.entity.vehicle.VehicleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.world.World;

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
	
	private static final Supplier<Reflection.MethodInvoker> PacketDecoder_decode =
			Reflection.getOptionalMethod(() -> Reflection.getClass("net.minecraft.class_9141"), () -> "decode", () -> MethodType.methodType(Object.class, Object.class));
	@SuppressWarnings("unchecked")
	public static <T> T packetCodecDecode(Object codec, Object buf) {
		return Version.<T>newSwitch()
				.range("1.20.5", null, () -> (T) PacketDecoder_decode.get().invoke(codec, buf))
				.range(null, "1.20.4", () -> { throw new IllegalStateException("Not supported in this version!"); })
				.get();
	}
	private static final Supplier<Reflection.MethodInvoker> PacketEncoder_encode =
			Reflection.getOptionalMethod(() -> Reflection.getClass("net.minecraft.class_9142"), () -> "encode", () -> MethodType.methodType(void.class, Object.class, Object.class));
	public static void packetCodecEncode(Object codec, Object buf, Object value) {
		Version.newSwitch()
				.range("1.20.5", null, () -> PacketEncoder_encode.get().invoke(codec, buf, value))
				.range(null, "1.20.4", () -> { throw new IllegalStateException("Not supported in this version!"); })
				.run();
	}
	
	private static final Supplier<Reflection.MethodInvoker> EntityType_create =
			Reflection.getOptionalMethod(EntityType.class, "method_5883", MethodType.methodType(Entity.class, World.class));
	public static Entity createEntity(EntityType<?> entityType, World world) {
		return Version.<Entity>newSwitch()
				.range("1.21.2", null, () -> entityType.create(world, SpawnReason.COMMAND))
				.range(null, "1.21.1", () -> EntityType_create.get().invoke(entityType, world))
				.get();
	}
	
	private static final Supplier<Reflection.MethodInvoker> Entity_hasPermissionLevel =
			Reflection.getOptionalMethod(Entity.class, "method_5687", MethodType.methodType(boolean.class, int.class));
	public static boolean hasPermissionLevel(PlayerEntity player, int level) {
		return Version.<Boolean>newSwitch()
				.range("1.21.2", null, () -> player.hasPermissionLevel(level))
				.range(null, "1.21.1", () -> Entity_hasPermissionLevel.get().invoke(player, level))
				.get();
	}
	
	private static final Supplier<Reflection.MethodInvoker> Property_getValues =
			Reflection.getOptionalMethod(Property.class, "method_11898", MethodType.methodType(Collection.class));
	public static <T extends Comparable<T>> Collection<T> getValues(Property<T> property) {
		return Version.<Collection<T>>newSwitch()
				.range("1.21.2", null, () -> property.getValues())
				.range(null, "1.21.1", () -> Property_getValues.get().invoke(property))
				.get();
	}
	
	private static final Supplier<Reflection.MethodInvoker> ServerWorld_getEntity =
			Reflection.getOptionalMethod(ServerWorld.class, "method_14190", MethodType.methodType(Entity.class, UUID.class));
	public static Entity getEntity(ServerWorld world, UUID uuid) {
		return Version.<Entity>newSwitch()
				.range("1.21.5", null, () -> world.getEntity(uuid))
				.range(null, "1.21.4", () -> ServerWorld_getEntity.get().invoke(world, uuid))
				.get();
	}
	
	private static final Supplier<Reflection.MethodInvoker> ScreenHandler_setPreviousCursorStack =
			Reflection.getOptionalMethod(ScreenHandler.class, "method_34250", MethodType.methodType(void.class, ItemStack.class));
	public static void setPreviousCursorStack(ScreenHandler handler, ItemStack item) {
		Version.newSwitch()
				.range("1.21.5", null, () -> handler.trackedCursorSlot.setReceivedStack(item))
				.range(null, "1.21.4", () -> ScreenHandler_setPreviousCursorStack.get().invoke(handler, item))
				.run();
	}
	
}
