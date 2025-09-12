package com.luneruniverse.minecraft.mod.nbteditor.server;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;

import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;

public class ServerMixinLink {
	
	public static final WeakHashMap<Slot, PlayerEntity> SLOT_OWNER = new WeakHashMap<>();
	public static final WeakHashMap<ServerPlayerEntity, Boolean> NO_SLOT_RESTRICTIONS_PLAYERS = new WeakHashMap<>();
	public static final WeakHashMap<BundleContentsComponent.Builder, Boolean> NO_SLOT_RESTRICTIONS_BUNDLES = new WeakHashMap<>();
	public static void slotCanInsertOrTake(Slot source, CallbackInfoReturnable<Boolean> info, boolean playerSlot) {
		if (!source.isEnabled())
			return;
		PlayerEntity owner = ServerMixinLink.SLOT_OWNER.get(source);
		if (owner == null)
			return;
		if (isNoSlotRestrictions(owner, playerSlot))
			info.setReturnValue(true);
	}
	public static boolean isNoSlotRestrictions(PlayerEntity player, boolean playerSlot) {
		if (player instanceof ServerPlayerEntity) {
			if (ServerMVMisc.hasPermissionLevel(player, 2) && NO_SLOT_RESTRICTIONS_PLAYERS.getOrDefault(player, false))
				return true;
		} else {
			if ((playerSlot ? NBTEditorClient.SERVER_CONN.isEditingAllowed() :
					NBTEditorClient.SERVER_CONN.isEditingExpanded()) && ConfigScreen.isNoSlotRestrictions()) {
				return true;
			}
		}
		return false;
	}
	
	
	public static final Set<Thread> BLOCK_ENTITY_WRITE_NBT_WITHOUT_IDENTIFYING_DATA = Collections.synchronizedSet(new HashSet<>());
	
	
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
