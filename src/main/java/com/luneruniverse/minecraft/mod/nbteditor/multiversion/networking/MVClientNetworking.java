package com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;

public class MVClientNetworking {
	
	public static class PlayNetworkStateEvents {
		public static interface Start {
			public static final Event<Start> EVENT = EventFactory.createArrayBacked(Start.class, listeners -> () -> {
				for (Start listener : listeners)
					listener.onPlayStart();
			});
			public void onPlayStart();
		}
		public static interface Join {
			public static final Event<Join> EVENT = EventFactory.createArrayBacked(Join.class, listeners -> () -> {
				for (Join listener : listeners)
					listener.onPlayJoin();
			});
			public void onPlayJoin();
		}
		public static interface Stop {
			public static final Event<Stop> EVENT = EventFactory.createArrayBacked(Stop.class, listeners -> () -> {
				for (Stop listener : listeners)
					listener.onPlayStop();
			});
			public void onPlayStop();
		}
	}
	
	private static final Map<Identifier, List<Consumer<MVPacket>>> listeners = new HashMap<>();
	
	public static void send(MVPacket packet) {
		MVMisc.sendC2SPacket(new CustomPayloadC2SPacket(packet));
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends MVPacket> void registerListener(Identifier id, Consumer<T> listener) {
		listeners.computeIfAbsent(id, key -> new ArrayList<>()).add(packet -> listener.accept((T) packet));
	}
	
	public static void callListeners(MVPacket packet) {
		List<Consumer<MVPacket>> specificListeners = listeners.get(packet.id());
		if (specificListeners == null)
			return;
		specificListeners.forEach(listener -> listener.accept(packet));
	}
	
}
