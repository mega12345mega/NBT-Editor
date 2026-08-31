package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.packets.ClickSlotC2SPacketParent;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.server.NBTEditorServer;

import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;

@Mixin(ClickSlotC2SPacket.class)
public class ClickSlotC2SPacketMixin_1_21_5 implements ClickSlotC2SPacketParent {
	private static final byte NO_SLOT_RESTRICTIONS_FLAG = 0b01000000;
	
	@ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/PacketCodec;tuple(Lnet/minecraft/network/codec/PacketCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/PacketCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/PacketCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/PacketCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/PacketCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/PacketCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/PacketCodec;Ljava/util/function/Function;Lcom/mojang/datafixers/util/Function7;)Lnet/minecraft/network/codec/PacketCodec;"), index = 7)
	private static Function<ClickSlotC2SPacket, Byte> clinit$PacketCodec_tuple(Function<ClickSlotC2SPacket, Byte> from4) {
		return packet -> {
			byte button = from4.apply(packet);
			if (packet.isNoSlotRestrictions())
				return (byte) (button | NO_SLOT_RESTRICTIONS_FLAG);
			return button;
		};
	}
	
	private boolean noSlotRestrictions;
	
	@ModifyVariable(method = "<init>", at = @At("CTOR_HEAD"))
	private byte init(byte button) {
		if (NBTEditorServer.isOnServerThread()) {
			if ((button & NO_SLOT_RESTRICTIONS_FLAG) != 0) {
				noSlotRestrictions = true;
				return (byte) (button & ~NO_SLOT_RESTRICTIONS_FLAG);
			}
			return button;
		} else {
			if (ConfigScreen.isNoSlotRestrictions() && NBTEditorClient.SERVER_CONN.isEditingExpanded())
				noSlotRestrictions = true;
			return button;
		}
	}
	
	@Override
	public boolean isNoSlotRestrictions() {
		return noSlotRestrictions;
	}
}
