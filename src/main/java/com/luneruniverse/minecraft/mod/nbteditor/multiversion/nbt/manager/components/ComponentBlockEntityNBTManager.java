package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.manager.components;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Attempt;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.manager.NBTManager;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.registry.DynamicRegistryManagerHolder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.ComponentMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;

public class ComponentBlockEntityNBTManager implements NBTManager<BlockEntity> {
	
	private static final Codec<ComponentMap> BlockEntity_Components_CODEC = Version.<Codec<ComponentMap>>newSwitch()
			.range("1.21.5", null, () -> BlockEntity.Components.CODEC.encoder())
			.range("1.20.5", "1.21.4", () -> Reflection.getField(BlockEntity.Components.class, "field_50176", "Lcom/mojang/serialization/Codec;"))
			.get();
	
	@Override
	public Attempt<NbtCompound> trySerialize(BlockEntity subject) {
		// Based on BlockEntity#createNbtWithId
		
		RegistryWrapper.WrapperLookup registryLookup = DynamicRegistryManagerHolder.get();
		
		NbtCompound output = new NbtCompound();
		subject.writeNbt(output, registryLookup);
		DataResult<NbtElement> result =
				BlockEntity_Components_CODEC.encodeStart(registryLookup.getOps(NbtOps.INSTANCE), subject.getComponents());
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
