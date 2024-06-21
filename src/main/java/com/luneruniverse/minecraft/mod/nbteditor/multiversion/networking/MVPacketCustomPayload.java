package com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;

/**
 * Used internally in multiversion.networking; DO NOT USE
 */
@Deprecated
public class MVPacketCustomPayload implements CustomPayload {
	
	/**
	 * Hides the {@link CustomPayload} in {@link CustomPayloadC2SPacket#CustomPayloadC2SPacket(CustomPayload)}
	 */
	public static CustomPayloadC2SPacket wrapC2S(MVPacket packet) {
		return new CustomPayloadC2SPacket(new MVPacketCustomPayload(packet));
	}
	/**
	 * Hides the {@link CustomPayload} in {@link CustomPayloadS2CPacket#CustomPayloadS2CPacket(CustomPayload)}
	 */
	public static CustomPayloadS2CPacket wrapS2C(MVPacket packet) {
		return new CustomPayloadS2CPacket(new MVPacketCustomPayload(packet));
	}
	
	/**
	 * Hides the {@link CustomPayload} in {@link CustomPayloadC2SPacket#payload()}
	 */
	public static MVPacket unwrapC2S(CustomPayloadC2SPacket packet) {
		if (packet.payload() instanceof MVPacketCustomPayload mvPacket)
			return mvPacket.getPacket();
		return null;
	}
	/**
	 * Hides the {@link CustomPayload} in {@link CustomPayloadS2CPacket#payload()}
	 */
	public static MVPacket unwrapS2C(CustomPayloadS2CPacket packet) {
		if (packet.payload() instanceof MVPacketCustomPayload mvPacket)
			return mvPacket.getPacket();
		return null;
	}
	
	private final MVPacket packet;
	
	public MVPacketCustomPayload(MVPacket packet) {
		this.packet = packet;
	}
	
	public MVPacket getPacket() {
		return packet;
	}
	
	@Override
	public void write(PacketByteBuf payload) {
		packet.write(payload);
	}
	
	@Override
	public Identifier id() {
		return packet.getPacketId();
	}
	
}
