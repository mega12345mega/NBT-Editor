package com.luneruniverse.minecraft.mod.nbteditor.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;

public class ServerMixinLink {
	
	public static final Map<Thread, PlayerEntity> SCREEN_HANDLER_OWNER = new HashMap<>();
	public static final WeakHashMap<Slot, PlayerEntity> SLOT_OWNER = new WeakHashMap<>();
	
	
	public static final List<Thread> BLOCK_ENTITY_WRITE_NBT_WITHOUT_IDENTIFYING_DATA = new ArrayList<>();
	
	
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
