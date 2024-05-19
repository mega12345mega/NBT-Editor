package com.luneruniverse.minecraft.mod.nbteditor.localnbt;

import java.util.Set;

import org.joml.Matrix4f;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class LocalEntity implements LocalNBT {
	
	private Identifier id;
	private NbtCompound nbt;
	
	public LocalEntity(Identifier id, NbtCompound nbt) {
		this.id = id;
		this.nbt = nbt;
	}
	
	@Override
	public boolean isEmpty(Identifier id) {
		return false;
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
		return MVRegistry.ENTITY_TYPE.get(id).getName().getString();
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
		return MVRegistry.ENTITY_TYPE.getIds();
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
	public NbtCompound getOrCreateNBT() {
		return nbt;
	}
	
	@Override
	public void renderIcon(MatrixStack matrices, int x, int y) {
		matrices.push();
		matrices.translate(0.0, 8.0, 0.0);
		LocalNBT.makeRotatingIcon(matrices, x, y, 0.75f);
		matrices.multiplyPositionMatrix(new Matrix4f().scaling(1, 1, -1));
		
		DiffuseLighting.method_34742();
		Entity entity = MVRegistry.ENTITY_TYPE.get(id).create(MainUtil.client.world);
		entity.readNbt(nbt);
		VertexConsumerProvider.Immediate provider = MVDrawableHelper.getDrawContext(matrices).getVertexConsumers();
		MainUtil.client.getEntityRenderDispatcher().getRenderer(entity).render(entity, 0, 0, matrices, provider, 255);
		provider.draw();
		
		matrices.pop();
	}
	
	@Override
	public LocalEntity copy() {
		return new LocalEntity(id, nbt.copy());
	}
	
	@Override
	public boolean equals(Object nbt) {
		if (nbt instanceof LocalEntity entity)
			return this.id.equals(entity.id) && this.nbt.equals(entity.nbt);
		return false;
	}
	
}
