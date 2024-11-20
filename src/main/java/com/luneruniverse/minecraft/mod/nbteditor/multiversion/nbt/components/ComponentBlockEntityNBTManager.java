package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.components;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.DynamicRegistryManagerHolder;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.Attempt;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.NBTManager;
import com.mojang.serialization.DataResult;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;

public class ComponentBlockEntityNBTManager implements NBTManager<BlockEntity> {
	
	@Override
	public Attempt<NbtCompound> trySerialize(BlockEntity subject) {
		// Based on BlockEntity#createNbtWithId
		
		RegistryWrapper.WrapperLookup registryLookup = DynamicRegistryManagerHolder.get();
		
		NbtCompound output = new NbtCompound();
		subject.writeNbt(output, registryLookup);
		DataResult<NbtElement> result = BlockEntity.Components.CODEC
				.encodeStart(registryLookup.getOps(NbtOps.INSTANCE), subject.getComponents());
		result.resultOrPartial().ifPresent(nbt -> output.copyFrom((NbtCompound) nbt));
		subject.writeIdToNbt(output);
		
		return new Attempt<>(output, result.error().map(DataResult.Error::message).orElse(null));
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
