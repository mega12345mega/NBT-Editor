package com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.DynamicRegistryManagerHolder;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IdentifierInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;

public class MVClientNetworking {
	
	public static class PlayNetworkStateEvents {
		private static final Identifier INTERNAL_PHASE = IdentifierInst.of("nbteditor", "networking");
		
		public static interface Start {
			public static final Event<Start> EVENT = EventFactory.createWithPhases(Start.class, listeners -> networkHandler -> {
				for (Start listener : listeners)
					listener.onPlayStart(networkHandler);
			}, INTERNAL_PHASE, Event.DEFAULT_PHASE);
			public void onPlayStart(ClientPlayNetworkHandler networkHandler);
		}
		public static interface Join {
			public static final Event<Join> EVENT = EventFactory.createWithPhases(Join.class, listeners -> () -> {
				for (Join listener : listeners)
					listener.onPlayJoin();
			}, INTERNAL_PHASE, Event.DEFAULT_PHASE);
			public void onPlayJoin();
		}
		public static interface Stop {
			public static final Event<Stop> EVENT = EventFactory.createWithPhases(Stop.class, listeners -> () -> {
				for (Stop listener : listeners)
					listener.onPlayStop();
			}, Event.DEFAULT_PHASE, INTERNAL_PHASE);
			public void onPlayStop();
		}
	}
	
	private static final Map<Identifier, List<Consumer<MVPacket>>> listeners = new HashMap<>();
	
	public static void init() {
		Version.newSwitch()
				.range("1.20.5", null, () -> {
					PlayNetworkStateEvents.Start.EVENT.register(PlayNetworkStateEvents.INTERNAL_PHASE, DynamicRegistryManagerHolder::setClientManager);
					PlayNetworkStateEvents.Stop.EVENT.register(PlayNetworkStateEvents.INTERNAL_PHASE, () -> DynamicRegistryManagerHolder.setClientManager(null));
				})
				.range(null, "1.20.4", () -> {})
				.run();
	}
	
	@SuppressWarnings("deprecation")
	public static void send(MVPacket packet) {
		MVMisc.sendC2SPacket(Version.<CustomPayloadC2SPacket>newSwitch()
				.range("1.20.2", null, () -> MVPacketCustomPayload.wrapC2S(packet))
				.range(null, "1.20.1", () -> {
					PacketByteBuf payload = new PacketByteBuf(Unpooled.buffer());
					packet.write(payload);
					try {
						return CustomPayloadC2SPacket.class.getConstructor(Identifier.class, PacketByteBuf.class)
								.newInstance(packet.getPacketId(), payload);
					} catch (Exception e) {
						throw new RuntimeException("Failed to create CustomPayloadC2SPacket", e);
					}
				})
				.get());
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends MVPacket> void registerListener(Identifier id, Consumer<T> listener) {
		listeners.computeIfAbsent(id, key -> new ArrayList<>()).add(packet -> listener.accept((T) packet));
	}
	
	public static void callListeners(MVPacket packet) {
		if (!MainUtil.client.isOnThread()) {
			MainUtil.client.execute(() -> callListeners(packet));
			return;
		}
		List<Consumer<MVPacket>> specificListeners = listeners.get(packet.getPacketId());
		if (specificListeners == null)
			return;
		specificListeners.forEach(listener -> listener.accept(packet));
	}
	
}
