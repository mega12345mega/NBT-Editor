package com.luneruniverse.minecraft.mod.nbteditor.server;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;

public class ServerMixinLink {
	
	public static final Map<Thread, PlayerEntity> SCREEN_HANDLER_OWNER = new ConcurrentHashMap<>();
	public static final WeakHashMap<Slot, PlayerEntity> SLOT_OWNER = new WeakHashMap<>();
	
	
	public static final Set<Thread> BLOCK_ENTITY_WRITE_NBT_WITHOUT_IDENTIFYING_DATA = Collections.synchronizedSet(new HashSet<>());
	
	
	public static final WeakHashMap<ServerPlayerEntity, Boolean> NO_SLOT_RESTRICTIONS_PLAYERS = new WeakHashMap<>();
	
	
	// Fake players show as a clientbound ClientConnection
	private static final Class<?> ClientPlayNetworkHandler;
	static {
		Class<?> ClientPlayNetworkHandler_holder;
		try {
			ClientPlayNetworkHandler_holder = Reflection.getClass("net.minecraft.class_634");
		} catch (RuntimeException e) {
			ClientPlayNetworkHandler_holder = null;
		}
		ClientPlayNetworkHandler = ClientPlayNetworkHandler_holder;
	}
	public static boolean isInstanceOfClientPlayNetworkHandlerSafely(PacketListener listener) {
		return ClientPlayNetworkHandler != null && ClientPlayNetworkHandler.isInstance(listener);
	}
	
}
