package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.ClientHandledScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.SetCursorItemS2CPacket;
import net.minecraft.network.packet.s2c.play.SetPlayerInventoryS2CPacket;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
	
	@Inject(method = "onSetCursorItem", at = @At("HEAD"), cancellable = true)
	private void onSetCursorItem(SetCursorItemS2CPacket packet, CallbackInfo info) {
		if (!MainUtil.client.isOnThread())
			return;
		
		if (NBTEditorClient.CURSOR_MANAGER.isBranched()) {
			info.cancel();
			
			if (!(NBTEditorClient.CURSOR_MANAGER.getCurrentRoot() instanceof CreativeInventoryScreen))
				MainUtil.client.player.currentScreenHandler.setCursorStack(packet.contents());
		}
	}
	
	@Inject(method = "onSetPlayerInventory", at = @At("RETURN"), cancellable = true)
	private void onSetPlayerInventory_return(SetPlayerInventoryS2CPacket packet, CallbackInfo info) {
		if (MainUtil.client.currentScreen instanceof ClientHandledScreen clientHandledScreen)
			clientHandledScreen.getServerInventoryManager().onSetPlayerInventoryPacket(packet);
	}
	
}
