package com.luneruniverse.minecraft.mod.nbteditor.packets;

import com.luneruniverse.minecraft.mod.nbteditor.misc.BlockStateProperties;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ViewBlockS2CPacket implements ResponsePacket {
	
	public static final Identifier ID = new Identifier("nbteditor", "view_block");
	
	private final int requestId;
	private final RegistryKey<World> world;
	private final BlockPos pos;
	private final Identifier id;
	private final BlockStateProperties state;
	private final NbtCompound nbt;
	
	public ViewBlockS2CPacket(int requestId, RegistryKey<World> world, BlockPos pos, Identifier id, BlockStateProperties state, NbtCompound nbt) {
		if ((world == null) != (pos == null))
			throw new IllegalArgumentException("world and pos have to be null together!");
		if ((id == null) != (state == null) || (id == null) != (nbt == null))
			throw new IllegalArgumentException("id, state, and nbt have to be null together!");
		
		this.requestId = requestId;
		this.world = world;
		this.pos = pos;
		this.id = id;
		this.state = state;
		this.nbt = nbt;
	}
	public ViewBlockS2CPacket(PacketByteBuf payload) {
		this.requestId = payload.readInt();
		if (payload.readBoolean()) {
			this.world = payload.readRegistryKey(payload.<World>readRegistryRefKey());
			this.pos = payload.readBlockPos();
		} else {
			this.world = null;
			this.pos = null;
		}
		if (payload.readBoolean()) {
			this.id = payload.readIdentifier();
			this.state = new BlockStateProperties(payload);
			this.nbt = payload.readNbt();
		} else {
			this.id = null;
			this.state = null;
			this.nbt = null;
		}
	}
	
	public int getRequestId() {
		return requestId;
	}
	public RegistryKey<World> getWorld() {
		return world;
	}
	public BlockPos getPos() {
		return pos;
	}
	public boolean foundBlock() {
		return id != null;
	}
	public Identifier getId() {
		return id;
	}
	public BlockStateProperties getState() {
		return state;
	}
	public NbtCompound getNbt() {
		return nbt;
	}
	
	@Override
	public void write(PacketByteBuf payload) {
		payload.writeInt(requestId);
		if (world == null) {
			payload.writeBoolean(false);
		} else {
			payload.writeBoolean(true);
			payload.writeIdentifier(world.getRegistry());
			payload.writeRegistryKey(world);
			payload.writeBlockPos(pos);
		}
		if (id == null) {
			payload.writeBoolean(false);
		} else {
			payload.writeBoolean(true);
			payload.writeIdentifier(id);
			state.writeToPayload(payload);
			payload.writeNbt(nbt);
		}
	}
	
	@Override
	public Identifier id() {
		return ID;
	}
	
}
