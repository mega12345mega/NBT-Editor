package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.PassContainerSlotUpdates;
import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.ClientHandledScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
	
	@Inject(method = "onScreenHandlerSlotUpdate", at = @At(value = "HEAD"), cancellable = true)
	private void onScreenHandlerSlotUpdate(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo info) {
		if (!MainUtil.client.isOnThread())
			return;
		
		if (packet.getSyncId() == ClientHandledScreen.SYNC_ID) {
			NBTEditor.LOGGER.warn("Ignoring a slot update packet with a ClientHandledScreen sync id!");
			info.cancel();
			return;
		}
		
		if (MainUtil.client.currentScreen instanceof PassContainerSlotUpdates) {
			info.cancel();
			
			if (packet.getSyncId() == -1) {
				MainUtil.client.player.currentScreenHandler.setCursorStack(packet.getStack());
				return;
			}
			
			if (packet.getSyncId() != 0) {
				if (packet.getSyncId() != MixinLink.LAST_SERVER_HANDLED_SCREEN.getScreenHandler().syncId) {
					NBTEditor.LOGGER.warn("Ignoring a slot update packet with a mismatched sync id! (sync id: " +
							packet.getSyncId() + ", slot: " + packet.getSlot() + ", item: " + packet.getStack() + ")");
					return;
				}
				MixinLink.LAST_SERVER_HANDLED_SCREEN.getScreenHandler().setStackInSlot(packet.getSlot(), packet.getRevision(), packet.getStack());
				return;
			}
			
			if (packet.getSlot() == 45)
				MainUtil.client.player.getInventory().setStack(40, packet.getStack());
			else if (packet.getSlot() < 9) {
				if (packet.getSlot() > 4)
					MainUtil.client.player.getInventory().armor.set(8 - packet.getSlot(), packet.getStack());
			} else
				MainUtil.client.player.getInventory().setStack(packet.getSlot() >= 36 ? packet.getSlot() - 36 : packet.getSlot(), packet.getStack());
		}
	}
	
}
