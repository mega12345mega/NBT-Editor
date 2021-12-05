package com.luneruniverse.minecraft.mod.nbteditor.mixin.source;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.screens.ClientContainerScreen;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.slot.SlotActionType;

@Mixin(ClientConnection.class)
public class SendPacketMixin {
	
	private static volatile boolean sendingSafe;
	
    @Inject(at = @At(value = "HEAD"), method = "sendImmediately", cancellable = true)
    private void sendImmediately(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> callback, CallbackInfo info) {
    	if (sendingSafe)
        	return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        
    	if (client.currentScreen instanceof ClientContainerScreen) {
    		if (packet instanceof CloseHandledScreenC2SPacket) {
    			info.cancel();
    			return;
    		}
	    	if (packet instanceof ClickSlotC2SPacket) {
	    		info.cancel();
	    		
	    		ClickSlotC2SPacket castedPacket = (ClickSlotC2SPacket) packet;
	    		
	    		new Thread(() -> {
	    			try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						return;
					}
	    			
		    		if (castedPacket.getActionType() == SlotActionType.PICKUP && castedPacket.getSlot() == -999)
		    			((ClientContainerScreen) client.currentScreen).throwCursor();
		    		else if (castedPacket.getActionType() == SlotActionType.THROW && castedPacket.getSlot() != -999)
		    			((ClientContainerScreen) client.currentScreen).throwSlot(castedPacket.getSlot(), castedPacket.getButton() == 1);
		    		
	    			sendingSafe = true;
	    			ClientContainerScreen.updateServerInventory();
	    			sendingSafe = false;
	    		}).start();
	    	}
    	}
    }
    
}
