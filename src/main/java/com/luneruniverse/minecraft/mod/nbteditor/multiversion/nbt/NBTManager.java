package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt;

import net.minecraft.nbt.NbtCompound;

/**
 * The NBT returned from the methods is a copy and the NBT passed into the methods will be copied
 */
public interface NBTManager<T> {
	public Attempt<NbtCompound> trySerialize(T subject);
	public default NbtCompound serialize(T subject, boolean requireSuccess) {
		Attempt<NbtCompound> attempt = trySerialize(subject);
		return requireSuccess ? attempt.getSuccessOrThrow() : attempt.getAttemptOrThrow();
	}
	
	/**
	 * Note: If this returns false, {@link #getNbt(T)} may still return an empty {@link NbtCompound} rather than null!
	 */
	public boolean hasNbt(T subject);
	public NbtCompound getNbt(T subject);
	public NbtCompound getOrCreateNbt(T subject);
	public void setNbt(T subject, NbtCompound nbt);
	
	public default String getNbtString(T subject) {
		NbtCompound nbt = getNbt(subject);
		if (nbt == null)
			return "";
		return nbt.asString();
	}
}
