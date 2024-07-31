package com.luneruniverse.minecraft.mod.nbteditor.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;

public class ServerMixinLink {
	
	public static final Map<Thread, PlayerEntity> SCREEN_HANDLER_OWNER = new HashMap<>();
	public static final WeakHashMap<Slot, PlayerEntity> SLOT_OWNER = new WeakHashMap<>();
	
	
	public static final List<Thread> BLOCK_ENTITY_WRITE_NBT_WITHOUT_IDENTIFYING_DATA = new ArrayList<>();
	
	
	public static final WeakHashMap<ServerPlayerEntity, Boolean> NO_SLOT_RESTRICTIONS_PLAYERS = new WeakHashMap<>();
	
}
