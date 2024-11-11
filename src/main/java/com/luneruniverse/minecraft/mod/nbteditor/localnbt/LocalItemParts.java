package com.luneruniverse.minecraft.mod.nbteditor.localnbt;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.NBTManagers;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class LocalItemParts extends LocalItem {
	
	private Item item;
	private NbtCompound nbt;
	private int count;
	
	private ItemStack cachedItem;
	private NbtCompound cachedNbt;
	
	public LocalItemParts(ItemStack item) {
		this.item = item.getItem();
		this.nbt = item.manager$getNbt();
		this.count = item.getCount();
		
		if (this.item == null)
			this.item = Items.AIR;
		
		this.cachedItem = MainUtil.copyAirable(item);
		this.cachedNbt = (this.nbt == null ? null : this.nbt.copy());
	}
	private LocalItemParts(LocalItemParts toCopy) {
		this.item = toCopy.item;
		this.nbt = (toCopy.nbt == null ? null : toCopy.nbt.copy());
		this.count = toCopy.count;
		this.cachedItem = MainUtil.copyAirable(toCopy.cachedItem);
		this.cachedNbt = (toCopy.cachedNbt == null ? null : toCopy.cachedNbt.copy());
	}
	
	@Override
	public LocalItemStack toStack() {
		return new LocalItemStack(getCachedItem());
	}
	@Override
	public LocalItemParts toParts() {
		return this;
	}
	
	private void setCachedItemCount() {
		Version.newSwitch()
				.range("1.21.0", null, () -> cachedItem.setCount(Math.min(count, cachedItem.getMaxCount())))
				.range(null, "1.20.6", () -> cachedItem.setCount(count))
				.run();
	}
	private ItemStack getCachedItem() {
		if (cachedItem.getItem() == item && Objects.equals(cachedNbt, nbt)) {
			setCachedItemCount();
			return cachedItem;
		}
		
		ItemStack oldCachedItem = cachedItem;
		cachedItem = new ItemStack(item, 1);
		cachedNbt = (nbt == null ? null : nbt.copy());
		try {
			cachedItem.manager$setNbt(cachedNbt);
		} catch (Exception e) {
			NBTEditor.LOGGER.warn("Error while updating item cache", e);
			cachedItem = oldCachedItem;
		}
		setCachedItemCount();
		return cachedItem;
	}
	@Override
	public ItemStack getEditableItem() {
		throw new UnsupportedOperationException("LocalItemParts's items cannot be edited directly!");
	}
	@Override
	public ItemStack getReadableItem() {
		return getCachedItem();
	}
	
	@Override
	public boolean isEmpty() {
		return item == Items.AIR || count <= 0;
	}
	@Override
	public boolean isEmpty(Identifier id) {
		return MVRegistry.ITEM.get(id) == Items.AIR;
	}
	
	@Override
	public Text getName() {
		return MainUtil.getCustomItemNameSafely(getCachedItem());
	}
	@Override
	public void setName(Text name) {
		if (NBTManagers.COMPONENTS_EXIST) {
			if (name == null) {
				if (nbt != null) {
					nbt.remove("custom_name");
					nbt.remove("minecraft:custom_name");
				}
			} else {
				NbtCompound nbt = getOrCreateNBT();
				nbt.putString(nbt.contains("minecraft:custom_name") || !nbt.contains("custom_name") ?
						"minecraft:custom_name" : "custom_name", TextInst.toJsonString(name));
			}
		} else {
			NbtCompound nbt = getOrCreateNBT();
			NbtCompound display = nbt.getCompound("display");
			if (name == null)
				display.remove("Name");
			else {
				display.putString("Name", TextInst.toJsonString(name));
				nbt.put("display", display);
			}
		}
	}
	@Override
	public String getDefaultName() {
		return MainUtil.getBaseItemNameSafely(getCachedItem()).getString();
	}
	
	@Override
	public Item getItemType() {
		return item;
	}
	@Override
	public Identifier getId() {
		return MVRegistry.ITEM.getId(item);
	}
	@Override
	public void setId(Identifier id) {
		item = MVRegistry.ITEM.get(id);
	}
	@Override
	public Set<Identifier> getIdOptions() {
		return MVRegistry.ITEM.getIds();
	}
	
	@Override
	public int getCount() {
		return count;
	}
	@Override
	public void setCount(int count) {
		this.count = count;
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
		if (nbt == null)
			nbt = new NbtCompound();
		return nbt;
	}
	
	@Override
	public void renderIcon(MatrixStack matrices, int x, int y) {
		MVDrawableHelper.renderItem(matrices, 200.0F, true, getCachedItem(), x, y);
	}
	
	@Override
	public Optional<ItemStack> toItem() {
		return Optional.of(getCachedItem().copy());
	}
	@Override
	public NbtCompound serialize() {
		NbtCompound output = new NbtCompound();
		output.putString("id", getId().toString());
		output.put(NBTManagers.COMPONENTS_EXIST ? "components" : "tag", nbt);
		output.putInt("count", count);
		output.putString("type", "item");
		return output;
	}
	@Override
	public Text toHoverableText() {
		return getCachedItem().toHoverableText();
	}
	
	@Override
	public LocalItemParts copy() {
		return new LocalItemParts(this);
	}
	@Override
	public boolean equals(Object nbt) {
		if (nbt instanceof LocalItemParts item)
			return this.item == item.item && Objects.equals(this.nbt, item.nbt) && this.count == item.count;
		return super.equals(nbt);
	}
	
}
