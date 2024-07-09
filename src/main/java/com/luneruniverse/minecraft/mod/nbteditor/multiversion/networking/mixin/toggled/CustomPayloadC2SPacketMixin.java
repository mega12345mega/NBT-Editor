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
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;

@SuppressWarnings("deprecation")
@Mixin(CustomPayloadC2SPacket.class)
public class CustomPayloadC2SPacketMixin {
	@Inject(method = "method_53026(Lnet/minecraft/class_2960;Lnet/minecraft/class_2540;)Lnet/minecraft/class_8710;", at = @At("HEAD"), cancellable = true, remap = false)
	private static void readPayload(Identifier id, PacketByteBuf payload, CallbackInfoReturnable<CustomPayload> info) {
		MVPacket packet = MVNetworking.readPacket(id, payload);
		if (packet != null)
			info.setReturnValue(new MVPacketCustomPayload(packet));
	}
}
