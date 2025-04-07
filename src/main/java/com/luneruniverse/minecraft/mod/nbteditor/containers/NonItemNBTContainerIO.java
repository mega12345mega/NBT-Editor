package com.luneruniverse.minecraft.mod.nbteditor.containers;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public interface NonItemNBTContainerIO {
	/**
	 * @param nbt May be null, which should result in 0 if it is not possible to determine the max size
	 */
	public int getMaxNBTSize(NbtCompound nbt, SourceContainerType source);
	public default boolean isNBTReadable(NbtCompound nbt, SourceContainerType source) {
		return true;
	}
	public ItemStack[] readNBT(NbtCompound container, SourceContainerType source);
	public int writeNBT(NbtCompound container, ItemStack[] contents, SourceContainerType source);
	
	public default NBTContainerIO withItemSupport(String defaultEntityId) {
		return new NBTContainerIO() {
			@Override
			public int getMaxNBTSize(NbtCompound nbt, SourceContainerType source) {
				return NonItemNBTContainerIO.this.getMaxNBTSize(nbt, source);
			}
			@Override
			public boolean isNBTReadable(NbtCompound nbt, SourceContainerType source) {
				return NonItemNBTContainerIO.this.isNBTReadable(nbt, source);
			}
			@Override
			public ItemStack[] readNBT(NbtCompound container, SourceContainerType source) {
				return NonItemNBTContainerIO.this.readNBT(container, source);
			}
			@Override
			public int writeNBT(NbtCompound container, ItemStack[] contents, SourceContainerType source) {
				return NonItemNBTContainerIO.this.writeNBT(container, contents, source);
			}
			
			@Override
			public String getDefaultEntityId() {
				return defaultEntityId;
			}
		};
	}
	public default NBTContainerIO withItemSupport(BlockEntityType<?> block) {
		return withItemSupport(BlockEntityType.getId(block).toString());
	}
	public default NBTContainerIO withItemSupport(EntityType<?> entity) {
		return withItemSupport(EntityType.getId(entity).toString());
	}
}
