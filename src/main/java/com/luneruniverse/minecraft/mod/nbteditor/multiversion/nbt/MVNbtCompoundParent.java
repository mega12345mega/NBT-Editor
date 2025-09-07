package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt;

import java.lang.invoke.MethodType;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Uuids;

public interface MVNbtCompoundParent {
	
	public static boolean NBT_CODE_REFACTORED = Version.<Boolean>newSwitch()
			.range("1.21.5", null, true)
			.range(null, "1.21.4", false)
			.get();
	
	public static final byte NUMBER_TYPE = 99;
	
	static final Supplier<Reflection.MethodInvoker> NbtCompound_contains =
			Reflection.getOptionalMethod(NbtCompound.class, "method_10573", MethodType.methodType(boolean.class, String.class, int.class));
	public default boolean nbte$contains(String key, byte type) {
		if (NBT_CODE_REFACTORED) {
			NbtElement value = ((NbtCompound) this).get(key);
			return value != null && (value.getType() == type || type == NUMBER_TYPE &&
					value.getType() >= NbtElement.BYTE_TYPE && value.getType() <= NbtElement.DOUBLE_TYPE);
		}
		return NbtCompound_contains.get().invoke(this, key, type);
	}
	
	static final Supplier<Reflection.MethodInvoker> NbtCompound_containsUuid =
			Reflection.getOptionalMethod(NbtCompound.class, "method_25928", MethodType.methodType(boolean.class, String.class));
	public default boolean nbte$containsUuid(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) this).get(key, Uuids.INT_STREAM_CODEC).isPresent();
		return NbtCompound_containsUuid.get().invoke(this, key);
	}
	
	public default Optional<Byte> nbte$getByte(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) this).getByte(key);
		if (nbte$contains(key, NUMBER_TYPE))
			return Optional.of(nbte$getByteOrDefault(key));
		return Optional.empty();
	}
	static final Supplier<Reflection.MethodInvoker> NbtCompound_getByte =
			Reflection.getOptionalMethod(NbtCompound.class, "method_10571", MethodType.methodType(byte.class, String.class));
	public default byte nbte$getByteOrDefault(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) this).getByte(key, (byte) 0);
		return NbtCompound_getByte.get().invoke(this, key);
	}
	
	public default Optional<Short> nbte$getShort(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) this).getShort(key);
		if (nbte$contains(key, NUMBER_TYPE))
			return Optional.of(nbte$getShortOrDefault(key));
		return Optional.empty();
	}
	static final Supplier<Reflection.MethodInvoker> NbtCompound_getShort =
			Reflection.getOptionalMethod(NbtCompound.class, "method_10568", MethodType.methodType(short.class, String.class));
	public default short nbte$getShortOrDefault(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) this).getShort(key, (short) 0);
		return NbtCompound_getShort.get().invoke(this, key);
	}
	
	public default Optional<Integer> nbte$getInt(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) this).getInt(key);
		if (nbte$contains(key, NUMBER_TYPE))
			return Optional.of(nbte$getIntOrDefault(key));
		return Optional.empty();
	}
	static final Supplier<Reflection.MethodInvoker> NbtCompound_getInt =
			Reflection.getOptionalMethod(NbtCompound.class, "method_10550", MethodType.methodType(int.class, String.class));
	public default int nbte$getIntOrDefault(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) this).getInt(key, 0);
		return NbtCompound_getInt.get().invoke(this, key);
	}
	
	public default Optional<Long> nbte$getLong(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) this).getLong(key);
		if (nbte$contains(key, NUMBER_TYPE))
			return Optional.of(nbte$getLongOrDefault(key));
		return Optional.empty();
	}
	static final Supplier<Reflection.MethodInvoker> NbtCompound_getLong =
			Reflection.getOptionalMethod(NbtCompound.class, "method_10537", MethodType.methodType(long.class, String.class));
	public default long nbte$getLongOrDefault(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) this).getLong(key, 0);
		return NbtCompound_getLong.get().invoke(this, key);
	}
	
	public default Optional<Float> nbte$getFloat(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) this).getFloat(key);
		if (nbte$contains(key, NUMBER_TYPE))
			return Optional.of(nbte$getFloatOrDefault(key));
		return Optional.empty();
	}
	static final Supplier<Reflection.MethodInvoker> NbtCompound_getFloat =
			Reflection.getOptionalMethod(NbtCompound.class, "method_10583", MethodType.methodType(float.class, String.class));
	public default float nbte$getFloatOrDefault(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) this).getFloat(key, 0);
		return NbtCompound_getFloat.get().invoke(this, key);
	}
	
	public default Optional<Double> nbte$getDouble(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) this).getDouble(key);
		if (nbte$contains(key, NUMBER_TYPE))
			return Optional.of(nbte$getDoubleOrDefault(key));
		return Optional.empty();
	}
	static final Supplier<Reflection.MethodInvoker> NbtCompound_getDouble =
			Reflection.getOptionalMethod(NbtCompound.class, "method_10574", MethodType.methodType(double.class, String.class));
	public default double nbte$getDoubleOrDefault(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) this).getDouble(key, 0);
		return NbtCompound_getDouble.get().invoke(this, key);
	}
	
	public default Optional<String> nbte$getString(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) this).getString(key);
		if (nbte$contains(key, NbtElement.STRING_TYPE))
			return Optional.of(nbte$getStringOrDefault(key));
		return Optional.empty();
	}
	static final Supplier<Reflection.MethodInvoker> NbtCompound_getString =
			Reflection.getOptionalMethod(NbtCompound.class, "method_10558", MethodType.methodType(String.class, String.class));
	public default String nbte$getStringOrDefault(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) this).getString(key, "");
		return NbtCompound_getString.get().invoke(this, key);
	}
	
	public default Optional<byte[]> nbte$getByteArray(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) this).getByteArray(key);
		if (nbte$contains(key, NbtElement.BYTE_ARRAY_TYPE))
			return Optional.of(nbte$getByteArrayOrDefault(key));
		return Optional.empty();
	}
	static final Supplier<Reflection.MethodInvoker> NbtCompound_getByteArray =
			Reflection.getOptionalMethod(NbtCompound.class, "method_10547", MethodType.methodType(byte[].class, String.class));
	public default byte[] nbte$getByteArrayOrDefault(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) this).getByteArray(key).orElseGet(() -> new byte[0]);
		return NbtCompound_getByteArray.get().invoke(this, key);
	}
	
	public default Optional<int[]> nbte$getIntArray(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) this).getIntArray(key);
		if (nbte$contains(key, NbtElement.INT_ARRAY_TYPE))
			return Optional.of(nbte$getIntArrayOrDefault(key));
		return Optional.empty();
	}
	static final Supplier<Reflection.MethodInvoker> NbtCompound_getIntArray =
			Reflection.getOptionalMethod(NbtCompound.class, "method_10561", MethodType.methodType(int[].class, String.class));
	public default int[] nbte$getIntArrayOrDefault(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) this).getIntArray(key).orElseGet(() -> new int[0]);
		return NbtCompound_getIntArray.get().invoke(this, key);
	}
	
	public default Optional<long[]> nbte$getLongArray(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) this).getLongArray(key);
		if (nbte$contains(key, NbtElement.LONG_ARRAY_TYPE))
			return Optional.of(nbte$getLongArrayOrDefault(key));
		return Optional.empty();
	}
	static final Supplier<Reflection.MethodInvoker> NbtCompound_getLongArray =
			Reflection.getOptionalMethod(NbtCompound.class, "method_10565", MethodType.methodType(long[].class, String.class));
	public default long[] nbte$getLongArrayOrDefault(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) this).getLongArray(key).orElseGet(() -> new long[0]);
		return NbtCompound_getLongArray.get().invoke(this, key);
	}
	
	public default Optional<NbtCompound> nbte$getCompound(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) this).getCompound(key);
		if (nbte$contains(key, NbtElement.COMPOUND_TYPE))
			return Optional.of(nbte$getCompoundOrDefault(key));
		return Optional.empty();
	}
	static final Supplier<Reflection.MethodInvoker> NbtCompound_getCompound =
			Reflection.getOptionalMethod(NbtCompound.class, "method_10562", MethodType.methodType(NbtCompound.class, String.class));
	public default NbtCompound nbte$getCompoundOrDefault(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) this).getCompoundOrEmpty(key);
		return NbtCompound_getCompound.get().invoke(this, key);
	}
	
	public default Optional<NbtList> nbte$getList(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) this).getList(key);
		if (nbte$contains(key, NbtElement.LIST_TYPE))
			return Optional.of(nbte$getListOrDefault(key));
		return Optional.empty();
	}
	public default NbtList nbte$getListOrDefault(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) this).getListOrEmpty(key);
		if (((NbtCompound) this).get(key) instanceof NbtList list)
			return list;
		return new NbtList();
	}
	public default Optional<NbtList> nbte$getList(String key, byte type) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) this).getList(key).filter(list -> list.stream().allMatch(element -> element.getType() == type));
		if (nbte$contains(key, NbtElement.LIST_TYPE)) {
			NbtList list = nbte$getListOrDefault(key);
			if (list.isEmpty() || list.nbte$getHeldType().get() == type)
				return Optional.of(list);
		}
		return Optional.empty();
	}
	static final Supplier<Reflection.MethodInvoker> NbtCompound_getList =
			Reflection.getOptionalMethod(NbtCompound.class, "method_10554", MethodType.methodType(NbtList.class, String.class, int.class));
	public default NbtList nbte$getListOrDefault(String key, byte type) {
		if (NBT_CODE_REFACTORED)
			return nbte$getList(key, type).orElseGet(NbtList::new);
		return NbtCompound_getList.get().invoke(this, key, type);
	}
	public default Optional<NbtList> nbte$getPartialList(String key, byte type) {
		if (NBT_CODE_REFACTORED) {
			return ((NbtCompound) this).getList(key).map(list -> list.stream().filter(element -> element.getType() == type)
					.collect(Collectors.toCollection(NbtList::new)));
		}
		return nbte$getList(key, type);
	}
	public default NbtList nbte$getPartialListOrDefault(String key, byte type) {
		if (NBT_CODE_REFACTORED)
			return nbte$getPartialList(key, type).orElseGet(NbtList::new);
		return nbte$getListOrDefault(key, type);
	}
	
	public default Optional<Boolean> nbte$getBoolean(String key) {
		return nbte$getByte(key).map(b -> b != 0);
	}
	public default boolean nbte$getBooleanOrDefault(String key) {
		return nbte$getByteOrDefault(key) != 0;
	}
	
	static final Supplier<Reflection.MethodInvoker> NbtCompound_getUuid =
			Reflection.getOptionalMethod(NbtCompound.class, "method_25926", MethodType.methodType(UUID.class, String.class));
	public default Optional<UUID> nbte$getUuid(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) this).get(key, Uuids.INT_STREAM_CODEC);
		if (nbte$containsUuid(key))
			return Optional.of(NbtCompound_getUuid.get().invoke(this, key));
		return Optional.empty();
	}
	
	static final Supplier<Reflection.MethodInvoker> NbtCompound_putUuid =
			Reflection.getOptionalMethod(NbtCompound.class, "method_25927", MethodType.methodType(void.class, String.class, UUID.class));
	public default void nbte$putUuid(String key, UUID uuid) {
		if (NBT_CODE_REFACTORED)
			((NbtCompound) this).put(key, Uuids.INT_STREAM_CODEC, uuid);
		else
			NbtCompound_putUuid.get().invoke(this, key, uuid);
	}
	
}
