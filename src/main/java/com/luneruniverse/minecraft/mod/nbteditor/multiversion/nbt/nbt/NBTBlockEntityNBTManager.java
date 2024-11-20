package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.nbt;

import java.lang.invoke.MethodType;
import java.util.function.Supplier;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.Attempt;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.NBTManager;
import com.luneruniverse.minecraft.mod.nbteditor.server.ServerMixinLink;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;

public class NBTBlockEntityNBTManager implements NBTManager<BlockEntity> {
	
	private static final Supplier<Reflection.MethodInvoker> BlockEntity_writeNbt =
			Reflection.getOptionalMethod(BlockEntity.class, "method_11007", MethodType.methodType(NbtCompound.class, NbtCompound.class));
	
	private static final Supplier<Reflection.MethodInvoker> BlockEntity_createNbtWithId =
			Reflection.getOptionalMethod(BlockEntity.class, "method_38243", MethodType.methodType(NbtCompound.class));
	@Override
	public Attempt<NbtCompound> trySerialize(BlockEntity subject) {
		return new Attempt<>(Version.<NbtCompound>newSwitch()
				.range("1.18.0", null, () -> BlockEntity_createNbtWithId.get().invoke(subject))
				.range(null, "1.17.1", () -> BlockEntity_writeNbt.get().invoke(subject, new NbtCompound()))
				.get());
	}
	
	@Override
	public boolean hasNbt(BlockEntity subject) {
		return true;
	}
	private static final Supplier<Reflection.MethodInvoker> BlockEntity_createNbt =
			Reflection.getOptionalMethod(BlockEntity.class, "method_38244", MethodType.methodType(NbtCompound.class));
	@Override
	public NbtCompound getNbt(BlockEntity subject) {
		return Version.<NbtCompound>newSwitch()
				.range("1.18.0", null, () -> BlockEntity_createNbt.get().invoke(subject))
				.range(null, "1.17.1", () -> {
					ServerMixinLink.BLOCK_ENTITY_WRITE_NBT_WITHOUT_IDENTIFYING_DATA.add(Thread.currentThread());
					return BlockEntity_writeNbt.get().invoke(subject, new NbtCompound());
				})
				.get();
	}
	@Override
	public NbtCompound getOrCreateNbt(BlockEntity subject) {
		return getNbt(subject);
	}
	private static final Reflection.MethodInvoker BlockEntity_readNbt =
			Reflection.getMethod(BlockEntity.class, "method_11014", MethodType.methodType(void.class, NbtCompound.class));
	@Override
	public void setNbt(BlockEntity subject, NbtCompound nbt) {
		BlockEntity_readNbt.invoke(subject, nbt);
	}
	
}
