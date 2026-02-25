package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.components;

import java.lang.invoke.MethodType;
import java.util.function.Supplier;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.DynamicRegistryManagerHolder;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.Attempt;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.NBTManager;
import com.mojang.serialization.DataResult;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.NbtReadView;

public class ComponentBlockEntityNBTManager implements NBTManager<BlockEntity> {

	// Pre-1.21.6: BlockEntity.writeNbt(NbtCompound, RegistryWrapper.WrapperLookup)
	private static final Supplier<Reflection.MethodInvoker> BlockEntity_writeNbt =
			Reflection.getOptionalMethod(BlockEntity.class, "method_45842",
					MethodType.methodType(void.class, NbtCompound.class, RegistryWrapper.WrapperLookup.class));
	// Pre-1.21.6: BlockEntity.writeIdToNbt(NbtCompound)
	private static final Supplier<Reflection.MethodInvoker> BlockEntity_writeIdToNbt =
			Reflection.getOptionalMethod(BlockEntity.class, "method_11000",
					MethodType.methodType(void.class, NbtCompound.class));
	// Pre-1.21.6: BlockEntity.read(NbtCompound, RegistryWrapper.WrapperLookup)
	private static final Supplier<Reflection.MethodInvoker> BlockEntity_read =
			Reflection.getOptionalMethod(BlockEntity.class, "method_11014",
					MethodType.methodType(void.class, NbtCompound.class, RegistryWrapper.WrapperLookup.class));

	@Override
	public Attempt<NbtCompound> trySerialize(BlockEntity subject) {
		return Version.<Attempt<NbtCompound>>newSwitch()
				.range("1.21.6", null, () -> {
					// In 1.21.6+, createNbtWithIdentifyingData includes all data
					RegistryWrapper.WrapperLookup registryLookup = DynamicRegistryManagerHolder.get();
					NbtCompound output = subject.createNbtWithIdentifyingData(registryLookup);
					return new Attempt<>(output, null);
				})
				.range(null, "1.21.5", () -> {
					// Based on BlockEntity#createNbtWithId (pre-1.21.6)
					RegistryWrapper.WrapperLookup registryLookup = DynamicRegistryManagerHolder.get();
					NbtCompound output = new NbtCompound();
					BlockEntity_writeNbt.get().invoke(subject, output, registryLookup);
					DataResult<NbtElement> result = BlockEntity.Components.CODEC
							.encodeStart(registryLookup.getOps(NbtOps.INSTANCE), subject.getComponents());
					result.resultOrPartial().ifPresent(nbt -> output.copyFrom((NbtCompound) nbt));
					BlockEntity_writeIdToNbt.get().invoke(subject, output);
					return new Attempt<>(output, result.error().map(DataResult.Error::message).orElse(null));
				})
				.get();
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
		RegistryWrapper.WrapperLookup registryLookup = DynamicRegistryManagerHolder.get();
		Version.newSwitch()
				.range("1.21.6", null, () -> {
					// In 1.21.6+, use NbtReadView to wrap NbtCompound for reading
					subject.read(NbtReadView.create(null, registryLookup, nbt));
				})
				.range(null, "1.21.5", () -> {
					BlockEntity_read.get().invoke(subject, nbt, registryLookup);
				})
				.run();
	}

}
