package com.luneruniverse.minecraft.mod.nbteditor.packets;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SummonEntityC2SPacket implements FabricPacket {
	
	public static final PacketType<SummonEntityC2SPacket> TYPE = PacketType.create(new Identifier("nbteditor", "summon_entity"), SummonEntityC2SPacket::new);
	
	private final int requestId;
	private final RegistryKey<World> world;
	private final Vec3d pos;
	private final Identifier id;
	private final NbtCompound nbt;
	
	public SummonEntityC2SPacket(int requestId, RegistryKey<World> world, Vec3d pos, Identifier id, NbtCompound nbt) {
		this.requestId = requestId;
		this.world = world;
		this.pos = pos;
		this.id = id;
		this.nbt = nbt;
	}
	public SummonEntityC2SPacket(PacketByteBuf payload) {
		this.requestId = payload.readInt();
		this.world = payload.readRegistryKey(payload.<World>readRegistryRefKey());
		this.pos = payload.readVec3d();
		this.id = payload.readIdentifier();
		this.nbt = payload.readNbt();
	}
	
	public int getRequestId() {
		return requestId;
	}
	public RegistryKey<World> getWorld() {
		return world;
	}
	public Vec3d getPos() {
		return pos;
	}
	public Identifier getId() {
		return id;
	}
	public NbtCompound getNbt() {
		return nbt;
	}
	
	@Override
	public void write(PacketByteBuf payload) {
		payload.writeInt(requestId);
		payload.writeIdentifier(world.getRegistry());
		payload.writeRegistryKey(world);
		payload.writeVec3d(pos);
		payload.writeIdentifier(id);
		payload.writeNbt(nbt);
	}
	
	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
	
}
