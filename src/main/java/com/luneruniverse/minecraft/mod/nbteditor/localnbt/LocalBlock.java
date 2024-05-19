package com.luneruniverse.minecraft.mod.nbteditor.localnbt;

import java.util.Set;

import com.luneruniverse.minecraft.mod.nbteditor.misc.BlockStateProperties;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public class LocalBlock implements LocalNBT {
	
	private Identifier id;
	private BlockStateProperties state;
	private NbtCompound nbt;
	
	public LocalBlock(Identifier id, BlockStateProperties state, NbtCompound nbt) {
		this.id = id;
		this.state = state;
		this.nbt = nbt;
	}
	
	public boolean isBlockEntity() {
		return MVRegistry.BLOCK.get(id) instanceof BlockEntityProvider;
	}
	
	@Override
	public boolean isEmpty(Identifier id) {
		return MVRegistry.BLOCK.get(id) == Blocks.AIR;
	}
	
	@Override
	public Text getName() {
		return MainUtil.getNbtNameSafely(nbt, "CustomName", () -> TextInst.of(getDefaultName()));
	}
	@Override
	public void setName(Text name) {
		if (name == null)
			getOrCreateNBT().remove("CustomName");
		else
			getOrCreateNBT().putString("CustomName", Text.Serialization.toJsonString(name));
	}
	@Override
	public String getDefaultName() {
		return MVRegistry.BLOCK.get(id).getName().getString();
	}
	
	@Override
	public Identifier getId() {
		return id;
	}
	@Override
	public void setId(Identifier id) {
		this.id = id;
		this.state = this.state.mapTo(MVRegistry.BLOCK.get(id).getDefaultState());
	}
	@Override
	public Set<Identifier> getIdOptions() {
		return MVRegistry.BLOCK.getIds();
	}
	
	public BlockStateProperties getState() {
		return state;
	}
	public void setState(BlockStateProperties state) {
		this.state = state;
	}
	
	@Override
	public NbtCompound getNBT() {
		return nbt;
	}
	@Override
	public void setNBT(NbtCompound nbt) {
		this.nbt = nbt;
	}
	
	@Override
	public void renderIcon(MatrixStack matrices, int x, int y) {
		RenderSystem.disableCull();
		
		matrices.push();
		LocalNBT.makeRotatingIcon(matrices, x, y, 1);
		matrices.translate(-0.5, -0.5, -0.5);
		VertexConsumerProvider.Immediate provider = MVDrawableHelper.getDrawContext(matrices).getVertexConsumers();
		
		Block block = MVRegistry.BLOCK.get(id);
		BlockState state = this.state.applyTo(block.getDefaultState());
		
		MainUtil.client.getBlockRenderManager().renderBlock(state, new BlockPos(0, 1000, 0), MainUtil.client.world, matrices,
				provider.getBuffer(RenderLayer.getCutout()), false, Random.create());
		if (block instanceof BlockEntityProvider entityProvider) {
			BlockEntity entity = entityProvider.createBlockEntity(new BlockPos(0, 1000, 0), state);
			entity.setWorld(MainUtil.client.world);
			entity.readNbt(nbt);
			MainUtil.client.getBlockEntityRenderDispatcher().renderEntity(entity, matrices, provider, 0xF000F0, OverlayTexture.DEFAULT_UV);
		}
		
		provider.draw();
		matrices.pop();
		
		RenderSystem.enableCull();
	}
	
	@Override
	public LocalBlock copy() {
		return new LocalBlock(id, state.copy(), nbt.copy());
	}
	
	@Override
	public boolean equals(Object nbt) {
		if (nbt instanceof LocalBlock block)
			return this.id.equals(block.id) && this.state.equals(block.state) && this.nbt.equals(block.nbt);
		return false;
	}
	
}
