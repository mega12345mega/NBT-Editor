package com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.mixin.client.toggled;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.util.StringHelper;

@Mixin(CommandExecutionC2SPacket.class)
public class CommandExecutionC2SPacketMixin {
	@ModifyVariable(method = "<init>(Ljava/lang/String;)V", at = @At("HEAD"), ordinal = 0)
	@Group(name = "<init>", min = 1)
	private static String command(String value) {
		return StringHelper.truncateChat(value);
	}
	@ModifyVariable(method = "<init>(Ljava/lang/String;Ljava/time/Instant;JLnet/minecraft/class_7450;Lnet/minecraft/class_7635$class_7636;)V", at = @At("HEAD"), ordinal = 0, remap = false)
	@Group(name = "<init>", min = 1)
	@SuppressWarnings("target")
	private static String command_old(String value) {
		return StringHelper.truncateChat(value);
	}
}
