package com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.mixin.toggled;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVNetworking;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVPacketCustomPayload;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Identifier;

@SuppressWarnings("deprecation")
@Mixin(targets = "net.minecraft.network.packet.CustomPayload$1")
public class CustomPayload1Mixin {
	@Inject(method = "getCodec", at = @At("HEAD"), cancellable = true)
	private void getCodec(Identifier id, CallbackInfoReturnable<PacketCodec<PacketByteBuf, MVPacketCustomPayload>> info) {
		if (!MVNetworking.isPacket(id))
			return;
		info.setReturnValue(PacketCodec.of((packet, payload) -> packet.getPacket().write(payload),
				payload -> new MVPacketCustomPayload(MVNetworking.readPacket(id, payload))));
	}
}
