package com.luneruniverse.minecraft.mod.nbteditor.localnbt;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVQuaternionf;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public interface LocalNBT {
	public static Optional<LocalNBT> deserialize(NbtCompound nbt, int defaultDataVersion) {
		return Optional.ofNullable(switch (nbt.contains("type", NbtElement.STRING_TYPE) ? nbt.getString("type") : "item") {
			case "item" -> LocalItemStack.deserialize(nbt, defaultDataVersion);
			case "block" -> LocalBlock.deserialize(nbt, defaultDataVersion);
			case "entity" -> LocalEntity.deserialize(nbt, defaultDataVersion);
			default -> null;
		});
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends LocalNBT> T copy(T localNBT) {
		return (T) localNBT.copy();
	}
	
	public static MVQuaternionf makeRotatingIcon(MatrixStack matrices, int x, int y, float scale, boolean inverse) {
		matrices.translate(x + 8, y + 8, 8.0);
		matrices.scale(scale, scale, scale);
		matrices.scale(12, 12, 12);
		
		MVQuaternionf quatX = MVQuaternionf.ofXRotation((float) (-Math.PI / 6));
		MVQuaternionf quatY = MVQuaternionf.ofYRotation((float) (System.currentTimeMillis() % 2000 / 2000.0f * Math.PI * 2));
		MVQuaternionf quatZ = MVQuaternionf.ofZRotation((float) Math.PI);
		
		if (inverse) {
			quatX.conjugate().applyToMatrixStack(matrices);
			quatY.copy().conjugate().applyToMatrixStack(matrices);
		} else {
			quatX.applyToMatrixStack(matrices);
			quatY.applyToMatrixStack(matrices);
		}
		quatZ.applyToMatrixStack(matrices);
		
		return quatY;
	}
	
	public default boolean isEmpty() {
		return isEmpty(getId());
	}
	public boolean isEmpty(Identifier id);
	
	public Text getName();
	public void setName(Text name);
	public String getDefaultName();
	
	public Identifier getId();
	public void setId(Identifier id);
	public Set<Identifier> getIdOptions();
	
	public NbtCompound getNBT();
	public void setNBT(NbtCompound nbt);
	public default NbtCompound getOrCreateNBT() {
		NbtCompound nbt = getNBT();
		if (nbt == null) {
			nbt = new NbtCompound();
			setNBT(nbt);
		}
		return nbt;
	}
	public default void modifyNBT(UnaryOperator<NbtCompound> modifier) {
		NbtCompound nbt = getNBT();
		if (nbt == null)
			nbt = new NbtCompound();
		setNBT(modifier.apply(nbt));
	}
	public default void modifyNBT(Consumer<NbtCompound> modifier) {
		modifyNBT(nbt -> {
			modifier.accept(nbt);
			return nbt;
		});
	}
	
	public void renderIcon(MatrixStack matrices, int x, int y);
	
	public Optional<ItemStack> toItem();
	public NbtCompound serialize();
	public Text toHoverableText();
	
	public LocalNBT copy();
	@Override
	public boolean equals(Object nbt);
}
