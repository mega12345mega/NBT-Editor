package com.luneruniverse.minecraft.mod.nbteditor.mixin.source;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.item.BoatItem;

@Mixin(BoatItem.class)
public interface BoatItemAccessor {
	@Accessor("chest")
	public boolean isChest();
}
