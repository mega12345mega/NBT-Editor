package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.components;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.DynamicRegistryManagerHolder;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.NBTManager;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;

public class ComponentBlockEntityNBTManager implements NBTManager<BlockEntity> {
	
	@Override
	public NbtCompound serialize(BlockEntity subject) {
		return subject.createNbtWithId(DynamicRegistryManagerHolder.get());
	}
	
	@Override
	public boolean hasNbt(BlockEntity subject) {
		return true;
	}
	@Override
	public NbtCompound getNbt(BlockEntity subject) {
		return subject.createNbt(DynamicRegistryManagerHolder.get());
	}
	@Override
	public NbtCompound getOrCreateNbt(BlockEntity subject) {
		return getNbt(subject);
	}
	@Override
	public void setNbt(BlockEntity subject, NbtCompound nbt) {
		subject.read(nbt, DynamicRegistryManagerHolder.get());
	}
	
}
