package com.luneruniverse.minecraft.mod.nbteditor.containers;

import java.util.function.Function;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public abstract class MultiTargetContainerIO extends ContainerIO {
	
	public enum Target {
		ENTITY(container -> container.getOrCreateSubNbt("EntityTag")),
		BLOCK_ENTITY(container -> container.getOrCreateSubNbt("BlockEntityTag")),
		ITEM(container -> container.getOrCreateNbt());
		
		private final Function<ItemStack, NbtCompound> target;
		private Target(Function<ItemStack, NbtCompound> target) {
			this.target = target;
		}
		public NbtCompound getItemsParent(ItemStack item) {
			return target.apply(item);
		}
	}
	
	protected final Target target;
	
	public MultiTargetContainerIO(Target target) {
		this.target = target;
	}
	
}
