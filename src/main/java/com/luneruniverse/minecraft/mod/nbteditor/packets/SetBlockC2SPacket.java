package com.luneruniverse.minecraft.mod.nbteditor.packets;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SetBlockC2SPacket implements FabricPacket {
	
	public static final PacketType<SetBlockC2SPacket> TYPE = PacketType.create(new Identifier("nbteditor", "set_block"), SetBlockC2SPacket::new);
	
	private final RegistryKey<World> world;
	private final BlockPos pos;
	private final Identifier id;
	private final NbtCompound nbt;
	
	public SetBlockC2SPacket(RegistryKey<World> world, BlockPos pos, Identifier id, NbtCompound nbt) {
		this.world = world;
		this.pos = pos;
		this.id = id;
		this.nbt = nbt;
	}
	public SetBlockC2SPacket(PacketByteBuf payload) {
		this.world = payload.readRegistryKey(payload.<World>readRegistryRefKey());
		this.pos = payload.readBlockPos();
		this.id = payload.readIdentifier();
		this.nbt = payload.readNbt();
	}
	
	public RegistryKey<World> getWorld() {
		return world;
	}
	public BlockPos getPos() {
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
		payload.writeIdentifier(world.getRegistry());
		payload.writeRegistryKey(world);
		payload.writeBlockPos(pos);
		payload.writeIdentifier(id);
		payload.writeNbt(nbt);
	}
	
	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
	
}
