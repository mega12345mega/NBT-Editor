package com.luneruniverse.minecraft.mod.nbteditor.localnbt;

import java.util.Optional;
import java.util.Set;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public interface LocalNBT {
	public static Optional<LocalNBT> deserialize(NbtCompound nbt) {
		return Optional.ofNullable(switch (nbt.contains("type", NbtElement.STRING_TYPE) ? nbt.getString("type") : "item") {
			case "item" -> LocalItem.deserialize(nbt);
			case "block" -> LocalBlock.deserialize(nbt);
			case "entity" -> LocalEntity.deserialize(nbt);
			default -> null;
		});
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends LocalNBT> T copy(T localNBT) {
		return (T) localNBT.copy();
	}
	
	public static void makeRotatingIcon(MatrixStack matrices, int x, int y, float scale) {
		matrices.translate(x + 8, y + 8, 8);
		matrices.scale(scale, scale, scale);
		matrices.scale(12, 12, 12);
		matrices.multiply(RotationAxis.POSITIVE_X.rotation((float) (-Math.PI / 6)));
		matrices.multiply(RotationAxis.POSITIVE_Y.rotation((float) (System.currentTimeMillis() % 2000 / 2000.0f * Math.PI * 2)));
		matrices.multiply(RotationAxis.POSITIVE_Z.rotation((float) Math.PI));
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
	
	public void renderIcon(MatrixStack matrices, int x, int y);
	
	public Optional<ItemStack> toItem();
	public NbtCompound serialize();
	public Text toHoverableText();
	
	public LocalNBT copy();
	public boolean equals(Object nbt);
}
