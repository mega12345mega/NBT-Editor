package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt;

import net.minecraft.nbt.NbtCompound;

public interface DeserializableNBTManager<T> extends NBTManager<T> {
	public Attempt<T> tryDeserialize(NbtCompound nbt);
	public default T deserialize(NbtCompound nbt, boolean requireSuccess) {
		Attempt<T> attempt = tryDeserialize(nbt);
		return requireSuccess ? attempt.getSuccessOrThrow() : attempt.getAttemptOrThrow();
	}
}
