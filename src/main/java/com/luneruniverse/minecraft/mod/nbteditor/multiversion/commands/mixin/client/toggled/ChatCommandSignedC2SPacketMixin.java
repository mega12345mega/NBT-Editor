package com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.mixin.client.toggled;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.network.packet.c2s.play.ChatCommandSignedC2SPacket;
import net.minecraft.util.StringHelper;

@Mixin(ChatCommandSignedC2SPacket.class)
public class ChatCommandSignedC2SPacketMixin {
	@ModifyVariable(method = "<init>(Ljava/lang/String;Ljava/time/Instant;JLnet/minecraft/network/message/ArgumentSignatureDataMap;Lnet/minecraft/network/message/LastSeenMessageList$Acknowledgment;)V", at = @At("HEAD"), ordinal = 0)
	private static String command(String value) {
		return StringHelper.truncateChat(value);
	}
}
