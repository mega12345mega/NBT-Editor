package com.luneruniverse.minecraft.mod.nbteditor.localnbt;

import java.util.Optional;
import java.util.Set;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.NBTManagers;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class LocalItem implements LocalNBT {
	
	public static LocalItem deserialize(NbtCompound nbt) {
		return new LocalItem(NBTManagers.ITEM.deserialize(nbt));
	}
	
	private ItemStack item;
	
	public LocalItem(ItemStack item) {
		this.item = item;
	}
	
	public ItemStack getItem() {
		return item;
	}
	
	@Override
	public boolean isEmpty() {
		return item.isEmpty();
	}
	@Override
	public boolean isEmpty(Identifier id) {
		return MVRegistry.ITEM.get(id) == Items.AIR;
	}
	
	@Override
	public Text getName() {
		return MainUtil.getItemNameSafely(item);
	}
	@Override
	public void setName(Text name) {
		item.manager$setCustomName(name);
	}
	@Override
	public String getDefaultName() {
		return item.getItem().getName().getString();
	}
	
	@Override
	public Identifier getId() {
		return MVRegistry.ITEM.getId(item.getItem());
	}
	@Override
	public void setId(Identifier id) {
		item = MainUtil.setType(MVRegistry.ITEM.get(id), item, item.getCount());
	}
	@Override
	public Set<Identifier> getIdOptions() {
		return MVRegistry.ITEM.getIds();
	}
	
	public int getCount() {
		return item.getCount();
	}
	public void setCount(int count) {
		item = MainUtil.setType(item.getItem(), item, count);
	}
	
	@Override
	public NbtCompound getNBT() {
		return item.manager$getNbt();
	}
	@Override
	public void setNBT(NbtCompound nbt) {
		item.manager$setNbt(nbt);
	}
	@Override
	public NbtCompound getOrCreateNBT() {
		return item.manager$getOrCreateNbt();
	}
	
	@Override
	public void renderIcon(MatrixStack matrices, int x, int y) {
		MVDrawableHelper.renderItem(matrices, 200.0F, true, item, x, y);
	}
	
	@Override
	public Optional<ItemStack> toItem() {
		return Optional.of(item.copy());
	}
	@Override
	public NbtCompound serialize() {
		NbtCompound output = item.manager$serialize();
		output.putString("type", "item");
		return output;
	}
	@Override
	public Text toHoverableText() {
		return item.toHoverableText();
	}
	
	public boolean receive() {
		if (item.isEmpty())
			return false;
		MainUtil.getWithMessage(item);
		return true;
	}
	
	@Override
	public LocalItem copy() {
		return new LocalItem(MainUtil.copyAirable(item));
	}
	@Override
	public boolean equals(Object nbt) {
		if (nbt instanceof LocalItem item)
			return ItemStack.areEqual(this.item, item.item);
		return false;
	}
	
}
