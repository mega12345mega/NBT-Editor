package com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.mixin;

import java.lang.invoke.MethodType;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVClientNetworking;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVNetworking;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVPacket;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVPacketCustomPayload;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVServerNetworking;
import com.luneruniverse.minecraft.mod.nbteditor.server.ClientLink;
import com.luneruniverse.minecraft.mod.nbteditor.server.NBTEditorServer;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
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
public class ClientConnectionMixin {
	@Shadow
	private NetworkSide side;
	@Shadow
	private PacketListener packetListener;
	@Shadow
	public boolean isOpen() { return false; }
	
	private PacketListener prevListener;
	
	@Inject(method = "setPacketListener", at = @At("HEAD"))
	@Group(name = "setPacketListener_head", min = 1)
	private void setPacketListener_head(NetworkState<?> state, PacketListener listener, CallbackInfo info) {
		prevListener = packetListener;
	}
	@Inject(method = "method_10763(Lnet/minecraft/class_2547;)V", at = @At("HEAD"), remap = false)
	@Group(name = "setPacketListener_head", min = 1)
	private void setPacketListener_head_old(PacketListener listener, CallbackInfo info) {
		prevListener = packetListener;
	}
	@Inject(method = "transitionInbound", at = @At("RETURN"))
	@Group(name = "setPacketListener_return", min = 1)
	private void transitionInbound_return(NetworkState<?> state, PacketListener listener, CallbackInfo info) {
		setPacketListener_return_impl(listener);
	}
	@Inject(method = "method_10763(Lnet/minecraft/class_2547;)V", at = @At("RETURN"), remap = false)
	@Group(name = "setPacketListener_return", min = 1)
	private void setPacketListener_return(PacketListener listener, CallbackInfo info) {
		setPacketListener_return_impl(listener);
	}
	private void setPacketListener_return_impl(PacketListener listener) {
		if (side == NetworkSide.CLIENTBOUND) {
			if (ClientLink.isInstanceOfClientPlayNetworkHandler(listener))
				MVClientNetworking.PlayNetworkStateEvents.Start.EVENT.invoker().onPlayStart();
			else if (ClientLink.isInstanceOfClientPlayNetworkHandler(prevListener))
				MVClientNetworking.PlayNetworkStateEvents.Stop.EVENT.invoker().onPlayStop();
		}
		if (side == NetworkSide.SERVERBOUND) {
			if (listener instanceof ServerPlayNetworkHandler handler)
				MVServerNetworking.PlayNetworkStateEvents.Start.EVENT.invoker().onPlayStart(handler.player);
			else if (prevListener instanceof ServerPlayNetworkHandler handler)
				MVServerNetworking.PlayNetworkStateEvents.Stop.EVENT.invoker().onPlayStop(handler.player);
		}
		prevListener = null;
	}
	
	@Inject(method = "disconnect", at = @At("HEAD"))
	private void disconnect(Text reason, CallbackInfo info) {
		if (isOpen()) {
			if (!NBTEditorServer.IS_DEDICATED && ClientLink.isInstanceOfClientPlayNetworkHandler(packetListener))
				MVClientNetworking.PlayNetworkStateEvents.Stop.EVENT.invoker().onPlayStop();
			if (packetListener instanceof ServerPlayNetworkHandler handler)
				MVServerNetworking.PlayNetworkStateEvents.Stop.EVENT.invoker().onPlayStop(handler.player);
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
		if (!NBTEditorServer.IS_DEDICATED && ClientLink.isInstanceOfClientPlayNetworkHandler(listener) && packet instanceof CustomPayloadS2CPacket customPacket) {
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
