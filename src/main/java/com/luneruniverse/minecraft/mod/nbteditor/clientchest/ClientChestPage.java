package com.luneruniverse.minecraft.mod.nbteditor.clientchest;

import java.util.Optional;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.DataVersionStatus;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

import net.minecraft.item.ItemStack;

public record ClientChestPage(Optional<Integer> dataVersion, ItemStack[] items, DynamicItems dynamicItems, PageLoadLevel loadLevel) {
	
	public static ClientChestPage unloaded() {
		return new ClientChestPage(Optional.empty(), null, null, PageLoadLevel.UNLOADED);
	}
	public static ClientChestPage unknownDataVersion() {
		return new ClientChestPage(Optional.empty(), null, null, PageLoadLevel.DYNAMIC_ITEMS);
	}
	public static ClientChestPage wrongDataVersion(int dataVersion) {
		return new ClientChestPage(Optional.of(dataVersion), null, null, PageLoadLevel.DYNAMIC_ITEMS);
	}
	
	public ClientChestPage {
		if (items != null && items.length != 54)
			throw new IllegalArgumentException("The number of items must be exactly 54");
	}
	public ClientChestPage(ItemStack[] items, DynamicItems dynamicItems, PageLoadLevel loadLevel) {
		this(Optional.of(Version.getDataVersion()), items, dynamicItems, loadLevel);
	}
	public ClientChestPage() {
		this(new ItemStack[54], new DynamicItems(), PageLoadLevel.DYNAMIC_ITEMS);
	}
	
	public boolean isInThisVersion() {
		return dataVersion.map(value -> value == Version.getDataVersion()).orElse(false);
	}
	public DataVersionStatus getDataVersionStatus() {
		return DataVersionStatus.of(dataVersion);
	}
	
	public ItemStack[] getItemsOrThrow() {
		if (isInThisVersion())
			return items;
		throw new IllegalStateException("Cannot get the items of a page in a different DataVersion!");
	}
	
}
