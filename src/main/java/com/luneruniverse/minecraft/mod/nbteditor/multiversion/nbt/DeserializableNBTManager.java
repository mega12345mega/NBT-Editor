package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt;

import net.minecraft.nbt.NbtCompound;

public interface DeserializableNBTManager<T> extends NBTManager<T> {
	public T deserialize(NbtCompound nbt);
}
