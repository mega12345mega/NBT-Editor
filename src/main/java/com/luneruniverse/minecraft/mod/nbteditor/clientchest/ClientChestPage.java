package com.luneruniverse.minecraft.mod.nbteditor.clientchest;

import java.util.Optional;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

import net.minecraft.item.ItemStack;

public record ClientChestPage(Optional<Integer> dataVersion, ItemStack[] items) {
	
	public ClientChestPage(ItemStack[] items) {
		this(Optional.of(Version.getDataVersion()), items);
	}
	
	public boolean isInThisVersion() {
		return dataVersion.map(value -> value == Version.getDataVersion()).orElse(false);
	}
	public boolean isOutdated(boolean includeUnknown) {
		return dataVersion.map(value -> value < Version.getDataVersion()).orElse(includeUnknown);
	}
	
	public ItemStack[] getItemsOrThrow() {
		if (isInThisVersion())
			return items;
		throw new IllegalStateException("Cannot get the items of a page in a different DataVersion!");
	}
	
}
