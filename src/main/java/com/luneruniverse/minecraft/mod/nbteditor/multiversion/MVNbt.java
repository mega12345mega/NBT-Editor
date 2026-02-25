package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.lang.invoke.MethodType;
import java.util.function.Supplier;

import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

/**
 * Wrapper methods for NbtCompound and NbtList that provide backward compatibility
 * across Minecraft versions. In 1.21.5+, many get methods changed to return Optional
 * or were renamed (getCompound → getCompoundOrEmpty, etc.).
 */
public class MVNbt {

	// ---- NbtCompound.getCompound(String) ----
	// Pre-1.21.5: returns NbtCompound directly (empty if missing)
	// 1.21.5+: returns Optional<NbtCompound>; getCompoundOrEmpty returns NbtCompound
	private static final Supplier<Reflection.MethodInvoker> NbtCompound_getCompound =
			Reflection.getOptionalMethod(NbtCompound.class, "method_10562",
					MethodType.methodType(NbtCompound.class, String.class));
	public static NbtCompound getCompound(NbtCompound nbt, String key) {
		return Version.<NbtCompound>newSwitch()
				.range("1.21.5", null, () -> nbt.getCompoundOrEmpty(key))
				.range(null, "1.21.4", () -> NbtCompound_getCompound.get().invoke(nbt, key))
				.get();
	}

	// ---- NbtCompound.getList(String, int) ----
	// Pre-1.21.5: returns NbtList directly (empty if missing)
	// 1.21.5+: returns Optional<NbtList>; getListOrEmpty returns NbtList
	private static final Supplier<Reflection.MethodInvoker> NbtCompound_getList =
			Reflection.getOptionalMethod(NbtCompound.class, "method_10554",
					MethodType.methodType(NbtList.class, String.class, int.class));
	public static NbtList getList(NbtCompound nbt, String key, int type) {
		return Version.<NbtList>newSwitch()
				.range("1.21.5", null, () -> nbt.getListOrEmpty(key, type))
				.range(null, "1.21.4", () -> NbtCompound_getList.get().invoke(nbt, key, type))
				.get();
	}

	// ---- NbtCompound.getInt(String) ----
	// Pre-1.21.5: returns int (0 if missing)
	// 1.21.5+: getInt(key, fallback) returns int with fallback
	private static final Supplier<Reflection.MethodInvoker> NbtCompound_getInt =
			Reflection.getOptionalMethod(NbtCompound.class, "method_10550",
					MethodType.methodType(int.class, String.class));
	public static int getInt(NbtCompound nbt, String key) {
		return Version.<Integer>newSwitch()
				.range("1.21.5", null, () -> nbt.getInt(key, 0))
				.range(null, "1.21.4", () -> (int) NbtCompound_getInt.get().invoke(nbt, key))
				.get();
	}

	// ---- NbtCompound.getLong(String) ----
	private static final Supplier<Reflection.MethodInvoker> NbtCompound_getLong =
			Reflection.getOptionalMethod(NbtCompound.class, "method_10537",
					MethodType.methodType(long.class, String.class));
	public static long getLong(NbtCompound nbt, String key) {
		return Version.<Long>newSwitch()
				.range("1.21.5", null, () -> nbt.getLong(key, 0L))
				.range(null, "1.21.4", () -> (long) NbtCompound_getLong.get().invoke(nbt, key))
				.get();
	}

	// ---- NbtCompound.getFloat(String) ----
	private static final Supplier<Reflection.MethodInvoker> NbtCompound_getFloat =
			Reflection.getOptionalMethod(NbtCompound.class, "method_10583",
					MethodType.methodType(float.class, String.class));
	public static float getFloat(NbtCompound nbt, String key) {
		return Version.<Float>newSwitch()
				.range("1.21.5", null, () -> nbt.getFloat(key, 0.0f))
				.range(null, "1.21.4", () -> (float) NbtCompound_getFloat.get().invoke(nbt, key))
				.get();
	}

	// ---- NbtCompound.getDouble(String) ----
	private static final Supplier<Reflection.MethodInvoker> NbtCompound_getDouble =
			Reflection.getOptionalMethod(NbtCompound.class, "method_10574",
					MethodType.methodType(double.class, String.class));
	public static double getDouble(NbtCompound nbt, String key) {
		return Version.<Double>newSwitch()
				.range("1.21.5", null, () -> nbt.getDouble(key, 0.0))
				.range(null, "1.21.4", () -> (double) NbtCompound_getDouble.get().invoke(nbt, key))
				.get();
	}

	// ---- NbtCompound.getByte(String) ----
	private static final Supplier<Reflection.MethodInvoker> NbtCompound_getByte =
			Reflection.getOptionalMethod(NbtCompound.class, "method_10571",
					MethodType.methodType(byte.class, String.class));
	public static byte getByte(NbtCompound nbt, String key) {
		return Version.<Byte>newSwitch()
				.range("1.21.5", null, () -> nbt.getByte(key, (byte) 0))
				.range(null, "1.21.4", () -> (byte) NbtCompound_getByte.get().invoke(nbt, key))
				.get();
	}

	// ---- NbtCompound.getShort(String) ----
	private static final Supplier<Reflection.MethodInvoker> NbtCompound_getShort =
			Reflection.getOptionalMethod(NbtCompound.class, "method_10568",
					MethodType.methodType(short.class, String.class));
	public static short getShort(NbtCompound nbt, String key) {
		return Version.<Short>newSwitch()
				.range("1.21.5", null, () -> nbt.getShort(key, (short) 0))
				.range(null, "1.21.4", () -> (short) NbtCompound_getShort.get().invoke(nbt, key))
				.get();
	}

	// ---- NbtCompound.getString(String) ----
	// Pre-1.21.5: returns String ("" if missing)
	// 1.21.5+: getString(key, fallback) returns String with fallback
	private static final Supplier<Reflection.MethodInvoker> NbtCompound_getString =
			Reflection.getOptionalMethod(NbtCompound.class, "method_10558",
					MethodType.methodType(String.class, String.class));
	public static String getString(NbtCompound nbt, String key) {
		return Version.<String>newSwitch()
				.range("1.21.5", null, () -> nbt.getString(key, ""))
				.range(null, "1.21.4", () -> (String) NbtCompound_getString.get().invoke(nbt, key))
				.get();
	}

	// ---- NbtCompound.contains(String, NUMBER_TYPE) replacement ----
	// Pre-1.21.5: used contains(key, NbtElement.NUMBER_TYPE) where NUMBER_TYPE=99
	// 1.21.5+: NUMBER_TYPE and type-aware contains removed; use instanceof AbstractNbtNumber
	private static final Supplier<Reflection.MethodInvoker> NbtCompound_contains_type =
			Reflection.getOptionalMethod(NbtCompound.class, "method_10541",
					MethodType.methodType(boolean.class, String.class, int.class));
	/**
	 * Check if the compound contains a numeric value at the given key.
	 * Replaces {@code nbt.contains(key, NbtElement.NUMBER_TYPE)}.
	 */
	public static boolean containsNumber(NbtCompound nbt, String key) {
		return Version.<Boolean>newSwitch()
				.range("1.21.5", null, () -> nbt.get(key) instanceof AbstractNbtNumber)
				.range(null, "1.21.4", () -> (boolean) NbtCompound_contains_type.get().invoke(nbt, key, 99))
				.get();
	}

	// ---- NbtList.getCompound(int) ----
	// Pre-1.21.5: returns NbtCompound directly (empty if out of bounds/wrong type)
	// 1.21.5+: returns Optional<NbtCompound>; getCompoundOrEmpty returns NbtCompound
	private static final Supplier<Reflection.MethodInvoker> NbtList_getCompound =
			Reflection.getOptionalMethod(NbtList.class, "method_10604",
					MethodType.methodType(NbtCompound.class, int.class));
	public static NbtCompound getCompound(NbtList list, int index) {
		return Version.<NbtCompound>newSwitch()
				.range("1.21.5", null, () -> list.getCompoundOrEmpty(index))
				.range(null, "1.21.4", () -> NbtList_getCompound.get().invoke(list, index))
				.get();
	}

	/**
	 * Check if an NbtElement is a number type.
	 * Replaces {@code element.getType() == NbtElement.NUMBER_TYPE}.
	 */
	public static boolean isNumberType(NbtElement element) {
		return element instanceof AbstractNbtNumber;
	}

}
