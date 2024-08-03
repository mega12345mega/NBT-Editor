package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.ClientHandledScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {
	
	private static volatile boolean sendingSafe;
	
	@Inject(at = @At(value = "HEAD"), method = "send(Lnet/minecraft/network/packet/Packet;)V", cancellable = true)
	private void send(Packet<?> packet, CallbackInfo info) {
		if (((ClientConnection) (Object) this).getSide() != NetworkSide.CLIENTBOUND)
			return;
		
		if (sendingSafe)
			return;
		
		if (MainUtil.client.currentScreen instanceof ClientHandledScreen) {
			if (packet instanceof ClickSlotC2SPacket slotPacket) {
				info.cancel();
				
				if (slotPacket.getActionType() == SlotActionType.PICKUP && slotPacket.getSlot() == -999)
					((ClientHandledScreen) MainUtil.client.currentScreen).throwCursor();
				else if (slotPacket.getActionType() == SlotActionType.THROW && slotPacket.getSlot() != -999)
					((ClientHandledScreen) MainUtil.client.currentScreen).throwSlot(slotPacket.getSlot(), slotPacket.getButton() == 1);
				
				sendingSafe = true;
				ClientHandledScreen.updateServerInventory();
				sendingSafe = false;
			}
		}
	}
	
}
