package com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.mixin.toggled;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVNetworking;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVPacket;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVPacketCustomPayload;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;

@SuppressWarnings("deprecation")
@Mixin(CustomPayloadS2CPacket.class)
public class CustomPayloadS2CPacketMixin {
	@Inject(method = "readPayload", at = @At("HEAD"), cancellable = true)
	private static void readPayload(Identifier id, PacketByteBuf payload, CallbackInfoReturnable<CustomPayload> info) {
		MVPacket packet = MVNetworking.readPacket(id, payload);
		if (packet != null)
			info.setReturnValue(new MVPacketCustomPayload(packet));
	}
}
