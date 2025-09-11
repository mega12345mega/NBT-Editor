package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.manager;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Attempt;

import net.minecraft.nbt.NbtCompound;

public interface DeserializableNBTManager<T> extends NBTManager<T> {
	public Attempt<T> tryDeserialize(NbtCompound nbt);
	public default T deserialize(NbtCompound nbt, boolean requireSuccess) throws IllegalStateException {
		Attempt<T> attempt = tryDeserialize(nbt);
		return requireSuccess ? attempt.getSuccessOrThrow() : attempt.getAttemptOrThrow();
	}
	public default T deserializeOrElse(NbtCompound nbt, T defaultValue) {
		return tryDeserialize(nbt).value().orElse(defaultValue);
	}
}
