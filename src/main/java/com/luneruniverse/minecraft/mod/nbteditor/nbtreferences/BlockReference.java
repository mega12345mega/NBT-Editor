package com.luneruniverse.minecraft.mod.nbteditor.nbtreferences;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalBlock;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.packets.GetBlockC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.SetBlockC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.ViewBlockS2CPacket;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockReference implements NBTReference<LocalBlock> {
	
	public static CompletableFuture<Optional<BlockReference>> getBlock(RegistryKey<World> world, BlockPos pos) {
		return NBTEditorClient.SERVER_CONN
				.sendRequest(requestId -> new GetBlockC2SPacket(requestId, world, pos), ViewBlockS2CPacket.class)
				.thenApply(optional -> optional.filter(ViewBlockS2CPacket::foundBlock)
						.map(packet -> new BlockReference(world, pos, packet.getId(), packet.getNbt())));
	}
	public static BlockReference getBlockWithoutNBT(BlockPos pos) {
		return new BlockReference(MainUtil.client.world.getRegistryKey(), pos,
				MVRegistry.BLOCK.getId(MainUtil.client.world.getBlockState(pos).getBlock()), new NbtCompound());
	}
	
	private final RegistryKey<World> world;
	private final BlockPos pos;
	private Identifier id;
	private NbtCompound nbt;
	
	public BlockReference(RegistryKey<World> world, BlockPos pos, Identifier id, NbtCompound nbt) {
		this.world = world;
		this.pos = pos;
		this.id = id;
		this.nbt = nbt;
	}
	
	public RegistryKey<World> getWorld() {
		return world;
	}
	public BlockPos getPos() {
		return pos;
	}
	
	@Override
	public LocalBlock getLocalNBT() {
		return new LocalBlock(id, nbt);
	}
	
	@Override
	public NbtCompound getNBT() {
		return nbt;
	}
	@Override
	public void saveNBT(Identifier id, NbtCompound toSave, Runnable onFinished) {
		this.id = id;
		this.nbt = toSave;
		ClientPlayNetworking.send(new SetBlockC2SPacket(world, pos, id, toSave));
		onFinished.run();
	}
	
	@Override
	public void showParent() {
		MainUtil.client.setScreen(null);
	}
	
}
