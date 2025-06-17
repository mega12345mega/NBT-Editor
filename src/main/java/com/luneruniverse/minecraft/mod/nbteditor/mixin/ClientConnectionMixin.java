package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.ClientHandledScreen;
import com.luneruniverse.minecraft.mod.nbteditor.server.NBTEditorServer;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin {
	
	@Shadow
	public abstract NetworkSide getSide();
	
	@Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
	private void send(Packet<?> packet, CallbackInfo info) {
		if (getSide() != NetworkSide.CLIENTBOUND)
			return;
		
		if (MainUtil.client.currentScreen instanceof ClientHandledScreen clientHandledScreen) {
			if (clientHandledScreen.getServerInventoryManager().isUpdatingServer())
				return;
			
			if (packet instanceof ClickSlotC2SPacket slotPacket) {
				info.cancel();
				NBTEditor.LOGGER.warn("Tried to send ClickSlotC2SPacket while not updating server inventory: slot=" +
						slotPacket.getSlot() + ", button=" + slotPacket.getButton() + ", action=" + slotPacket.getActionType());
			}
		}
	}
	
	@Inject(method = "<init>", at = @At("HEAD"))
	private static void init(NetworkSide side, CallbackInfo info) {
		// When on a dedicated server, all threads are already server threads
		if (side == NetworkSide.SERVERBOUND)
			NBTEditorServer.registerServerThread(Thread.currentThread());
	}
	
}
