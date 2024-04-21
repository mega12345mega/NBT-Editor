package com.luneruniverse.minecraft.mod.nbteditor.localnbt;

import java.util.Set;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.block.Blocks;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class LocalBlock implements LocalNBT {
	
	private Identifier id;
	private NbtCompound nbt;
	
	public LocalBlock(Identifier id, NbtCompound nbt) {
		this.id = id;
		this.nbt = nbt;
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
	}
	@Override
	public Set<Identifier> getIdOptions() {
		return MVRegistry.BLOCK.getIds();
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
		MainUtil.client.getBlockRenderManager().renderBlockAsEntity(MVRegistry.BLOCK.get(id).getDefaultState(), matrices, provider, 255, OverlayTexture.DEFAULT_UV);
		provider.draw();
		
		matrices.pop();
		
		RenderSystem.enableCull();
	}
	
	@Override
	public LocalBlock copy() {
		return new LocalBlock(id, nbt.copy());
	}
	
	@Override
	public boolean equals(Object nbt) {
		if (nbt instanceof LocalBlock block)
			return this.id.equals(block.id) && this.nbt.equals(block.nbt);
		return false;
	}
	
}
