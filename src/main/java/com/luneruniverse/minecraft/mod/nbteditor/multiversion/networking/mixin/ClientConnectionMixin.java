package com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.mixin;

import java.lang.invoke.MethodType;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVClientNetworking;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVNetworking;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVPacket;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVPacketCustomPayload;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVServerNetworking;
import com.luneruniverse.minecraft.mod.nbteditor.server.NBTEditorServer;
import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMixinLink;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@SuppressWarnings("deprecation")
@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin {
	@Shadow
	private PacketListener packetListener;
	@Shadow
	public abstract boolean isOpen();
	
	@Inject(method = "disconnect", at = @At("HEAD"))
	private void disconnect(Text reason, CallbackInfo info) {
		if (isOpen()) {
			if (!NBTEditorServer.IS_DEDICATED && ServerMixinLink.isInstanceOfClientPlayNetworkHandlerSafely(packetListener))
				MVClientNetworking.onPlayStop();
			if (packetListener instanceof ServerPlayNetworkHandler handler)
				MVServerNetworking.onPlayStop(handler.player);
		}
	}
	
	private static final Supplier<Reflection.MethodInvoker> CustomPayloadS2CPacket_getChannel =
			Reflection.getOptionalMethod(CustomPayloadS2CPacket.class, "method_11456", MethodType.methodType(Identifier.class));
	private static final Supplier<Reflection.MethodInvoker> CustomPayloadS2CPacket_getData =
			Reflection.getOptionalMethod(CustomPayloadS2CPacket.class, "method_11458", MethodType.methodType(PacketByteBuf.class));
	private static final Supplier<Reflection.MethodInvoker> CustomPayloadC2SPacket_getChannel =
			Reflection.getOptionalMethod(CustomPayloadC2SPacket.class, "method_36169", MethodType.methodType(Identifier.class));
	private static final Supplier<Reflection.MethodInvoker> CustomPayloadC2SPacket_getData =
			Reflection.getOptionalMethod(CustomPayloadC2SPacket.class, "method_36170", MethodType.methodType(PacketByteBuf.class));
	@Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
	private static void handlePacket(Packet<?> packet, PacketListener listener, CallbackInfo info) {
		if (!NBTEditorServer.IS_DEDICATED && ServerMixinLink.isInstanceOfClientPlayNetworkHandlerSafely(listener) && packet instanceof CustomPayloadS2CPacket customPacket) {
			MVPacket mvPacket = Version.<MVPacket>newSwitch()
					.range("1.20.2", null, () -> MVPacketCustomPayload.unwrapS2C(customPacket))
					.range(null, "1.20.1", () -> MVNetworking.readPacket(
							CustomPayloadS2CPacket_getChannel.get().invoke(customPacket),
							CustomPayloadS2CPacket_getData.get().invoke(customPacket)))
					.get();
			if (mvPacket != null) {
				MVClientNetworking.callListeners(mvPacket);
				info.cancel();
			}
		}
		if (listener instanceof ServerPlayNetworkHandler handler && packet instanceof CustomPayloadC2SPacket customPacket) {
			MVPacket mvPacket = Version.<MVPacket>newSwitch()
					.range("1.20.2", null, () -> MVPacketCustomPayload.unwrapC2S(customPacket))
					.range(null, "1.20.1", () -> MVNetworking.readPacket(
							CustomPayloadC2SPacket_getChannel.get().invoke(customPacket),
							CustomPayloadC2SPacket_getData.get().invoke(customPacket)))
					.get();
			if (mvPacket != null) {
				MVServerNetworking.callListeners(mvPacket, handler.player);
				info.cancel();
			}
		}
	}
}
