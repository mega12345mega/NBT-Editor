package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IgnoreCloseScreenPacket;
import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.ClientHandledScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.ClientScreenHandler;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
	
	private static boolean updatingClientInventory;
	
	@Inject(method = "onInventory", at = @At("HEAD"), cancellable = true)
	private void onInventory(InventoryS2CPacket packet, CallbackInfo info) {
		if (!MainUtil.client.isOnThread() || updatingClientInventory)
			return;
		
		if (packet.getSyncId() == ClientScreenHandler.SYNC_ID) {
			NBTEditor.LOGGER.warn("Ignoring an inventory packet with a ClientHandledScreen sync id!");
			info.cancel();
			return;
		}
		
		if (NBTEditorClient.CURSOR_MANAGER.isBranched()) {
			info.cancel();
			
			try {
				updatingClientInventory = true;
				MainUtil.client.player.currentScreenHandler = NBTEditorClient.CURSOR_MANAGER.getCurrentRoot().getScreenHandler();
				((ClientPlayNetworkHandler) (Object) this).onInventory(packet);
			} finally {
				updatingClientInventory = false;
				MainUtil.client.player.currentScreenHandler = NBTEditorClient.CURSOR_MANAGER.getCurrentBranch().getScreenHandler();
			}
		}
	}
	
	@Inject(method = "onScreenHandlerSlotUpdate", at = @At("HEAD"), cancellable = true)
	private void onScreenHandlerSlotUpdate(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo info) {
		if (!MainUtil.client.isOnThread() || updatingClientInventory)
			return;
		
		if (packet.getSyncId() == ClientScreenHandler.SYNC_ID) {
			NBTEditor.LOGGER.warn("Ignoring a slot update packet with a ClientHandledScreen sync id!");
			info.cancel();
			return;
		}
		
		if (NBTEditorClient.CURSOR_MANAGER.isBranched()) {
			info.cancel();
			
			if (packet.getSyncId() == -1) {
				if (!(NBTEditorClient.CURSOR_MANAGER.getCurrentRoot() instanceof CreativeInventoryScreen))
					MainUtil.client.player.currentScreenHandler.setCursorStack(packet.getStack());
				return;
			}
			
			try {
				updatingClientInventory = true;
				MainUtil.client.player.currentScreenHandler = NBTEditorClient.CURSOR_MANAGER.getCurrentRoot().getScreenHandler();
				((ClientPlayNetworkHandler) (Object) this).onScreenHandlerSlotUpdate(packet);
			} finally {
				updatingClientInventory = false;
				MainUtil.client.player.currentScreenHandler = NBTEditorClient.CURSOR_MANAGER.getCurrentBranch().getScreenHandler();
			}
		}
	}
	
	@Inject(method = "onInventory", at = @At("RETURN"), cancellable = true)
	private void onInventory_return(InventoryS2CPacket packet, CallbackInfo info) {
		if (MainUtil.client.currentScreen instanceof ClientHandledScreen clientHandledScreen)
			clientHandledScreen.getServerInventoryManager().onInventoryPacket(packet);
	}
	
	@Inject(method = "onScreenHandlerSlotUpdate", at = @At("RETURN"), cancellable = true)
	private void onScreenHandlerSlotUpdate_return(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo info) {
		if (MainUtil.client.currentScreen instanceof ClientHandledScreen clientHandledScreen)
			clientHandledScreen.getServerInventoryManager().onScreenHandlerSlotUpdatePacket(packet);
	}
	
	@Inject(method = "onCloseScreen", at = @At("HEAD"), cancellable = true)
	private void onCloseScreen(CloseScreenS2CPacket packet, CallbackInfo info) {
		if (!MainUtil.client.isOnThread())
			return;
		
		if (packet.getSyncId() == ClientScreenHandler.SYNC_ID) {
			NBTEditor.LOGGER.warn("Ignoring a close screen packet with a ClientHandledScreen sync id!");
			info.cancel();
			return;
		}
		
		NBTEditorClient.CURSOR_MANAGER.onCloseScreenPacket();
		
		if (MainUtil.client.currentScreen instanceof IgnoreCloseScreenPacket)
			info.cancel();
	}
	
}
