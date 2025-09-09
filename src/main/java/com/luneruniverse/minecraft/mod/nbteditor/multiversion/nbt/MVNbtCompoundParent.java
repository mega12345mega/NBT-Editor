package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt;

import java.util.Optional;
import java.util.UUID;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

public interface MVNbtCompoundParent {
	
	public static boolean NBT_CODE_REFACTORED = Version.<Boolean>newSwitch()
			.range("1.21.5", null, true)
			.range(null, "1.21.4", false)
			.get();
	
	public static final byte NUMBER_TYPE = 99;
	
	public default boolean nbte$contains(String key, byte type) {
		throw new RuntimeException("Missing implementation for MVNbtCompoundParent#nbte$contains");
	}
	
	public default boolean nbte$containsUuid(String key) {
		throw new RuntimeException("Missing implementation for MVNbtCompoundParent#nbte$containsUuid");
	}
	
	public default Optional<Byte> nbte$getByte(String key) {
		throw new RuntimeException("Missing implementation for MVNbtCompoundParent#nbte$getByte");
	}
	public default byte nbte$getByteOrDefault(String key) {
		throw new RuntimeException("Missing implementation for MVNbtCompoundParent#nbte$getByteOrDefault");
	}
	
	public default Optional<Short> nbte$getShort(String key) {
		throw new RuntimeException("Missing implementation for MVNbtCompoundParent#nbte$getShort");
	}
	public default short nbte$getShortOrDefault(String key) {
		throw new RuntimeException("Missing implementation for MVNbtCompoundParent#nbte$getShortOrDefault");
	}
	
	public default Optional<Integer> nbte$getInt(String key) {
		throw new RuntimeException("Missing implementation for MVNbtCompoundParent#nbte$getInt");
	}
	public default int nbte$getIntOrDefault(String key) {
		throw new RuntimeException("Missing implementation for MVNbtCompoundParent#nbte$getIntOrDefault");
	}
	
	public default Optional<Long> nbte$getLong(String key) {
		throw new RuntimeException("Missing implementation for MVNbtCompoundParent#nbte$getLong");
	}
	public default long nbte$getLongOrDefault(String key) {
		throw new RuntimeException("Missing implementation for MVNbtCompoundParent#nbte$getLongOrDefault");
	}
	
	public default Optional<Float> nbte$getFloat(String key) {
		throw new RuntimeException("Missing implementation for MVNbtCompoundParent#nbte$getFloat");
	}
	public default float nbte$getFloatOrDefault(String key) {
		throw new RuntimeException("Missing implementation for MVNbtCompoundParent#nbte$getFloatOrDefault");
	}
	
	public default Optional<Double> nbte$getDouble(String key) {
		throw new RuntimeException("Missing implementation for MVNbtCompoundParent#nbte$getDouble");
	}
	public default double nbte$getDoubleOrDefault(String key) {
		throw new RuntimeException("Missing implementation for MVNbtCompoundParent#nbte$getDoubleOrDefault");
	}
	
	public default Optional<String> nbte$getString(String key) {
		throw new RuntimeException("Missing implementation for MVNbtCompoundParent#nbte$getString");
	}
	public default String nbte$getStringOrDefault(String key) {
		throw new RuntimeException("Missing implementation for MVNbtCompoundParent#nbte$getStringOrDefault");
	}
	
	public default Optional<byte[]> nbte$getByteArray(String key) {
		throw new RuntimeException("Missing implementation for MVNbtCompoundParent#nbte$getByteArray");
	}
	public default byte[] nbte$getByteArrayOrDefault(String key) {
		throw new RuntimeException("Missing implementation for MVNbtCompoundParent#nbte$getByteArrayOrDefault");
	}
	
	public default Optional<int[]> nbte$getIntArray(String key) {
		throw new RuntimeException("Missing implementation for MVNbtCompoundParent#nbte$getIntArray");
	}
	public default int[] nbte$getIntArrayOrDefault(String key) {
		throw new RuntimeException("Missing implementation for MVNbtCompoundParent#nbte$getIntArrayOrDefault");
	}
	
	public default Optional<long[]> nbte$getLongArray(String key) {
		throw new RuntimeException("Missing implementation for MVNbtCompoundParent#nbte$getLongArray");
	}
	public default long[] nbte$getLongArrayOrDefault(String key) {
		throw new RuntimeException("Missing implementation for MVNbtCompoundParent#nbte$getLongArrayOrDefault");
	}
	
	public default Optional<NbtCompound> nbte$getCompound(String key) {
		throw new RuntimeException("Missing implementation for MVNbtCompoundParent#nbte$getCompound");
	}
	public default NbtCompound nbte$getCompoundOrDefault(String key) {
		throw new RuntimeException("Missing implementation for MVNbtCompoundParent#nbte$getCompoundOrDefault");
	}
	
	public default Optional<NbtList> nbte$getList(String key) {
		throw new RuntimeException("Missing implementation for MVNbtCompoundParent#nbte$getList");
	}
	public default NbtList nbte$getListOrDefault(String key) {
		throw new RuntimeException("Missing implementation for MVNbtCompoundParent#nbte$getListOrDefault");
	}
	public default Optional<NbtList> nbte$getList(String key, byte type) {
		throw new RuntimeException("Missing implementation for MVNbtCompoundParent#nbte$getList");
	}
	public default NbtList nbte$getListOrDefault(String key, byte type) {
		throw new RuntimeException("Missing implementation for MVNbtCompoundParent#nbte$getListOrDefault");
	}
	public default Optional<NbtList> nbte$getPartialList(String key, byte type) {
		throw new RuntimeException("Missing implementation for MVNbtCompoundParent#nbte$getPartialList");
	}
	public default NbtList nbte$getPartialListOrDefault(String key, byte type) {
		throw new RuntimeException("Missing implementation for MVNbtCompoundParent#nbte$getPartialListOrDefault");
	}
	
	public default Optional<Boolean> nbte$getBoolean(String key) {
		throw new RuntimeException("Missing implementation for MVNbtCompoundParent#nbte$getBoolean");
	}
	public default boolean nbte$getBooleanOrDefault(String key) {
		throw new RuntimeException("Missing implementation for MVNbtCompoundParent#nbte$getBooleanOrDefault");
	}
	
	public default Optional<UUID> nbte$getUuid(String key) {
		throw new RuntimeException("Missing implementation for MVNbtCompoundParent#nbte$getUuid");
	}
	
	public default void nbte$putUuid(String key, UUID uuid) {
		throw new RuntimeException("Missing implementation for MVNbtCompoundParent#nbte$putUuid");
	}
	
}
