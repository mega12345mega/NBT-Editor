package com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class MVServerNetworking {
	
	public static class PlayNetworkStateEvents {
		public static interface Start {
			public static final Event<Start> EVENT = EventFactory.createArrayBacked(Start.class, listeners -> player -> {
				for (Start listener : listeners)
					listener.onPlayStart(player);
			});
			public void onPlayStart(ServerPlayerEntity player);
		}
		public static interface Stop {
			public static final Event<Stop> EVENT = EventFactory.createArrayBacked(Stop.class, listeners -> player -> {
				for (Stop listener : listeners)
					listener.onPlayStop(player);
			});
			public void onPlayStop(ServerPlayerEntity player);
		}
	}
	
	private static final Map<Identifier, List<BiConsumer<MVPacket, ServerPlayerEntity>>> listeners = new HashMap<>();
	
	@SuppressWarnings("deprecation")
	public static void send(ServerPlayerEntity player, MVPacket packet) {
		MVMisc.sendS2CPacket(player, Version.<CustomPayloadS2CPacket>newSwitch()
				.range("1.20.2", null, () -> MVPacketCustomPayload.wrapS2C(packet))
				.range(null, "1.20.1", () -> {
					PacketByteBuf payload = new PacketByteBuf(Unpooled.buffer());
					packet.write(payload);
					try {
						return CustomPayloadS2CPacket.class.getConstructor(Identifier.class, PacketByteBuf.class)
								.newInstance(packet.getPacketId(), payload);
					} catch (Exception e) {
						throw new RuntimeException("Failed to create CustomPayloadS2CPacket", e);
					}
				})
				.get());
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends MVPacket> void registerListener(Identifier id, BiConsumer<T, ServerPlayerEntity> listener) {
		listeners.computeIfAbsent(id, key -> new ArrayList<>()).add((packet, player) -> listener.accept((T) packet, player));
	}
	
	public static void callListeners(MVPacket packet, ServerPlayerEntity player) {
		List<BiConsumer<MVPacket, ServerPlayerEntity>> specificListeners = listeners.get(packet.getPacketId());
		if (specificListeners == null)
			return;
		specificListeners.forEach(listener -> listener.accept(packet, player));
	}
	
}
