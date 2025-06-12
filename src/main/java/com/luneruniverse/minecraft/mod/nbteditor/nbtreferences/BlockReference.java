package com.luneruniverse.minecraft.mod.nbteditor.nbtreferences;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalBlock;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVClientNetworking;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.GetBlockC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.GetLecternBlockC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.SetBlockC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.packets.ViewBlockS2CPacket;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.BlockStateProperties;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockReference implements NBTReference<LocalBlock> {
	
	private static CompletableFuture<Optional<BlockReference>> getBlock(Function<Integer, MVPacket> packetFactory) {
		return NBTEditorClient.SERVER_CONN
				.sendRequest(packetFactory, ViewBlockS2CPacket.class)
				.thenApply(optional -> optional.filter(ViewBlockS2CPacket::foundBlock)
						.map(packet -> new BlockReference(packet.getWorld(), packet.getPos(),
								MVRegistry.BLOCK.get(packet.getId()), packet.getState(), packet.getNbt())));
	}
	public static CompletableFuture<Optional<BlockReference>> getBlock(RegistryKey<World> world, BlockPos pos) {
		return getBlock(requestId -> new GetBlockC2SPacket(requestId, world, pos));
	}
	public static CompletableFuture<Optional<BlockReference>> getLecternBlock() {
		return getBlock(GetLecternBlockC2SPacket::new);
	}
	public static BlockReference getBlockWithoutNBT(BlockPos pos) {
		BlockState state = MainUtil.client.world.getBlockState(pos);
		return new BlockReference(MainUtil.client.world.getRegistryKey(), pos,
				state.getBlock(), new BlockStateProperties(state), new NbtCompound());
	}
	
	private final RegistryKey<World> world;
	private final BlockPos pos;
	private Block block;
	private BlockStateProperties state;
	private NbtCompound nbt;
	
	public BlockReference(RegistryKey<World> world, BlockPos pos, Block block, BlockStateProperties state, NbtCompound nbt) {
		this.world = world;
		this.pos = pos;
		this.block = block;
		this.state = state;
		this.nbt = nbt;
	}
	
	public RegistryKey<World> getWorld() {
		return world;
	}
	public BlockPos getPos() {
		return pos;
	}
	
	@Override
	public boolean exists() {
		return true;
	}
	
	@Override
	public LocalBlock getLocalNBT() {
		return new LocalBlock(block, state, nbt);
	}
	@Override
	public void saveLocalNBT(LocalBlock block, Runnable onFinished) {
		this.block = block.getBlock();
		this.state = block.getState();
		this.nbt = block.getNBT();
		MVClientNetworking.send(new SetBlockC2SPacket(world, pos, block.getId(), state, nbt,
				ConfigScreen.isRecreateBlocksAndEntities(), ConfigScreen.isTriggerBlockUpdates()));
		onFinished.run();
	}
	
	@Override
	public Identifier getId() {
		return MVRegistry.BLOCK.getId(block);
	}
	@Override
	public NbtCompound getNBT() {
		return nbt;
	}
	@Override
	public void saveNBT(Identifier id, NbtCompound toSave, Runnable onFinished) {
		this.block = MVRegistry.BLOCK.get(id);
		this.nbt = toSave;
		MVClientNetworking.send(new SetBlockC2SPacket(world, pos, id, state, toSave,
				ConfigScreen.isRecreateBlocksAndEntities(), ConfigScreen.isTriggerBlockUpdates()));
		onFinished.run();
	}
	
	public Block getBlock() {
		return block;
	}
	
	public BlockStateProperties getState() {
		return state;
	}
	public void saveState(BlockStateProperties state, Runnable onFinished) {
		this.state = state;
		MVClientNetworking.send(new SetBlockC2SPacket(world, pos, MVRegistry.BLOCK.getId(block), state, nbt,
				ConfigScreen.isRecreateBlocksAndEntities(), ConfigScreen.isTriggerBlockUpdates()));
		onFinished.run();
	}
	public void saveState(BlockStateProperties state, Text msg) {
		saveState(state, () -> MainUtil.client.player.sendMessage(msg, false));
	}
	public void saveState(BlockStateProperties state) {
		saveState(state, () -> {});
	}
	
}
