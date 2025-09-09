package com.luneruniverse.minecraft.mod.nbteditor.multiversion.mixin;

import java.lang.invoke.MethodType;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.spongepowered.asm.mixin.Mixin;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.MVNbtCompoundParent;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Uuids;

@Mixin(NbtCompound.class)
public class NbtCompoundMixin implements MVNbtCompoundParent {
	
	private static final Supplier<Reflection.MethodInvoker> NbtCompound_contains =
			Reflection.getOptionalMethod(NbtCompound.class, "method_10573", MethodType.methodType(boolean.class, String.class, int.class));
	public boolean nbte$contains(String key, byte type) {
		if (NBT_CODE_REFACTORED) {
			NbtElement value = ((NbtCompound) (Object) this).get(key);
			return value != null && (value.getType() == type || type == NUMBER_TYPE &&
					value.getType() >= NbtElement.BYTE_TYPE && value.getType() <= NbtElement.DOUBLE_TYPE);
		}
		return NbtCompound_contains.get().invoke(this, key, type);
	}
	
	private static final Supplier<Reflection.MethodInvoker> NbtCompound_containsUuid =
			Reflection.getOptionalMethod(NbtCompound.class, "method_25928", MethodType.methodType(boolean.class, String.class));
	public boolean nbte$containsUuid(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) (Object) this).get(key, Uuids.INT_STREAM_CODEC).isPresent();
		return NbtCompound_containsUuid.get().invoke(this, key);
	}
	
	public Optional<Byte> nbte$getByte(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) (Object) this).getByte(key);
		if (nbte$contains(key, NUMBER_TYPE))
			return Optional.of(nbte$getByteOrDefault(key));
		return Optional.empty();
	}
	private static final Supplier<Reflection.MethodInvoker> NbtCompound_getByte =
			Reflection.getOptionalMethod(NbtCompound.class, "method_10571", MethodType.methodType(byte.class, String.class));
	public byte nbte$getByteOrDefault(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) (Object) this).getByte(key, (byte) 0);
		return NbtCompound_getByte.get().invoke(this, key);
	}
	
	public Optional<Short> nbte$getShort(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) (Object) this).getShort(key);
		if (nbte$contains(key, NUMBER_TYPE))
			return Optional.of(nbte$getShortOrDefault(key));
		return Optional.empty();
	}
	private static final Supplier<Reflection.MethodInvoker> NbtCompound_getShort =
			Reflection.getOptionalMethod(NbtCompound.class, "method_10568", MethodType.methodType(short.class, String.class));
	public short nbte$getShortOrDefault(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) (Object) this).getShort(key, (short) 0);
		return NbtCompound_getShort.get().invoke(this, key);
	}
	
	public Optional<Integer> nbte$getInt(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) (Object) this).getInt(key);
		if (nbte$contains(key, NUMBER_TYPE))
			return Optional.of(nbte$getIntOrDefault(key));
		return Optional.empty();
	}
	private static final Supplier<Reflection.MethodInvoker> NbtCompound_getInt =
			Reflection.getOptionalMethod(NbtCompound.class, "method_10550", MethodType.methodType(int.class, String.class));
	public int nbte$getIntOrDefault(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) (Object) this).getInt(key, 0);
		return NbtCompound_getInt.get().invoke(this, key);
	}
	
	public Optional<Long> nbte$getLong(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) (Object) this).getLong(key);
		if (nbte$contains(key, NUMBER_TYPE))
			return Optional.of(nbte$getLongOrDefault(key));
		return Optional.empty();
	}
	private static final Supplier<Reflection.MethodInvoker> NbtCompound_getLong =
			Reflection.getOptionalMethod(NbtCompound.class, "method_10537", MethodType.methodType(long.class, String.class));
	public long nbte$getLongOrDefault(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) (Object) this).getLong(key, 0);
		return NbtCompound_getLong.get().invoke(this, key);
	}
	
	public Optional<Float> nbte$getFloat(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) (Object) this).getFloat(key);
		if (nbte$contains(key, NUMBER_TYPE))
			return Optional.of(nbte$getFloatOrDefault(key));
		return Optional.empty();
	}
	private static final Supplier<Reflection.MethodInvoker> NbtCompound_getFloat =
			Reflection.getOptionalMethod(NbtCompound.class, "method_10583", MethodType.methodType(float.class, String.class));
	public float nbte$getFloatOrDefault(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) (Object) this).getFloat(key, 0);
		return NbtCompound_getFloat.get().invoke(this, key);
	}
	
	public Optional<Double> nbte$getDouble(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) (Object) this).getDouble(key);
		if (nbte$contains(key, NUMBER_TYPE))
			return Optional.of(nbte$getDoubleOrDefault(key));
		return Optional.empty();
	}
	private static final Supplier<Reflection.MethodInvoker> NbtCompound_getDouble =
			Reflection.getOptionalMethod(NbtCompound.class, "method_10574", MethodType.methodType(double.class, String.class));
	public double nbte$getDoubleOrDefault(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) (Object) this).getDouble(key, 0);
		return NbtCompound_getDouble.get().invoke(this, key);
	}
	
	public Optional<String> nbte$getString(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) (Object) this).getString(key);
		if (nbte$contains(key, NbtElement.STRING_TYPE))
			return Optional.of(nbte$getStringOrDefault(key));
		return Optional.empty();
	}
	private static final Supplier<Reflection.MethodInvoker> NbtCompound_getString =
			Reflection.getOptionalMethod(NbtCompound.class, "method_10558", MethodType.methodType(String.class, String.class));
	public String nbte$getStringOrDefault(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) (Object) this).getString(key, "");
		return NbtCompound_getString.get().invoke(this, key);
	}
	
	public Optional<byte[]> nbte$getByteArray(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) (Object) this).getByteArray(key);
		if (nbte$contains(key, NbtElement.BYTE_ARRAY_TYPE))
			return Optional.of(nbte$getByteArrayOrDefault(key));
		return Optional.empty();
	}
	private static final Supplier<Reflection.MethodInvoker> NbtCompound_getByteArray =
			Reflection.getOptionalMethod(NbtCompound.class, "method_10547", MethodType.methodType(byte[].class, String.class));
	public byte[] nbte$getByteArrayOrDefault(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) (Object) this).getByteArray(key).orElseGet(() -> new byte[0]);
		return NbtCompound_getByteArray.get().invoke(this, key);
	}
	
	public Optional<int[]> nbte$getIntArray(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) (Object) this).getIntArray(key);
		if (nbte$contains(key, NbtElement.INT_ARRAY_TYPE))
			return Optional.of(nbte$getIntArrayOrDefault(key));
		return Optional.empty();
	}
	private static final Supplier<Reflection.MethodInvoker> NbtCompound_getIntArray =
			Reflection.getOptionalMethod(NbtCompound.class, "method_10561", MethodType.methodType(int[].class, String.class));
	public int[] nbte$getIntArrayOrDefault(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) (Object) this).getIntArray(key).orElseGet(() -> new int[0]);
		return NbtCompound_getIntArray.get().invoke(this, key);
	}
	
	public Optional<long[]> nbte$getLongArray(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) (Object) this).getLongArray(key);
		if (nbte$contains(key, NbtElement.LONG_ARRAY_TYPE))
			return Optional.of(nbte$getLongArrayOrDefault(key));
		return Optional.empty();
	}
	private static final Supplier<Reflection.MethodInvoker> NbtCompound_getLongArray =
			Reflection.getOptionalMethod(NbtCompound.class, "method_10565", MethodType.methodType(long[].class, String.class));
	public long[] nbte$getLongArrayOrDefault(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) (Object) this).getLongArray(key).orElseGet(() -> new long[0]);
		return NbtCompound_getLongArray.get().invoke(this, key);
	}
	
	public Optional<NbtCompound> nbte$getCompound(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) (Object) this).getCompound(key);
		if (nbte$contains(key, NbtElement.COMPOUND_TYPE))
			return Optional.of(nbte$getCompoundOrDefault(key));
		return Optional.empty();
	}
	private static final Supplier<Reflection.MethodInvoker> NbtCompound_getCompound =
			Reflection.getOptionalMethod(NbtCompound.class, "method_10562", MethodType.methodType(NbtCompound.class, String.class));
	public NbtCompound nbte$getCompoundOrDefault(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) (Object) this).getCompoundOrEmpty(key);
		return NbtCompound_getCompound.get().invoke(this, key);
	}
	
	public Optional<NbtList> nbte$getList(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) (Object) this).getList(key);
		if (nbte$contains(key, NbtElement.LIST_TYPE))
			return Optional.of(nbte$getListOrDefault(key));
		return Optional.empty();
	}
	public NbtList nbte$getListOrDefault(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) (Object) this).getListOrEmpty(key);
		if (((NbtCompound) (Object) this).get(key) instanceof NbtList list)
			return list;
		return new NbtList();
	}
	public Optional<NbtList> nbte$getList(String key, byte type) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) (Object) this).getList(key).filter(list -> list.stream().allMatch(element -> element.getType() == type));
		if (nbte$contains(key, NbtElement.LIST_TYPE)) {
			NbtList list = nbte$getListOrDefault(key);
			if (list.isEmpty() || list.nbte$getHeldType().get() == type)
				return Optional.of(list);
		}
		return Optional.empty();
	}
	private static final Supplier<Reflection.MethodInvoker> NbtCompound_getList =
			Reflection.getOptionalMethod(NbtCompound.class, "method_10554", MethodType.methodType(NbtList.class, String.class, int.class));
	public NbtList nbte$getListOrDefault(String key, byte type) {
		if (NBT_CODE_REFACTORED)
			return nbte$getList(key, type).orElseGet(NbtList::new);
		return NbtCompound_getList.get().invoke(this, key, type);
	}
	public Optional<NbtList> nbte$getPartialList(String key, byte type) {
		if (NBT_CODE_REFACTORED) {
			return ((NbtCompound) (Object) this).getList(key).map(list -> list.stream().filter(element -> element.getType() == type)
					.collect(Collectors.toCollection(NbtList::new)));
		}
		return nbte$getList(key, type);
	}
	public NbtList nbte$getPartialListOrDefault(String key, byte type) {
		if (NBT_CODE_REFACTORED)
			return nbte$getPartialList(key, type).orElseGet(NbtList::new);
		return nbte$getListOrDefault(key, type);
	}
	
	public Optional<Boolean> nbte$getBoolean(String key) {
		return nbte$getByte(key).map(b -> b != 0);
	}
	public boolean nbte$getBooleanOrDefault(String key) {
		return nbte$getByteOrDefault(key) != 0;
	}
	
	private static final Supplier<Reflection.MethodInvoker> NbtCompound_getUuid =
			Reflection.getOptionalMethod(NbtCompound.class, "method_25926", MethodType.methodType(UUID.class, String.class));
	public Optional<UUID> nbte$getUuid(String key) {
		if (NBT_CODE_REFACTORED)
			return ((NbtCompound) (Object) this).get(key, Uuids.INT_STREAM_CODEC);
		if (nbte$containsUuid(key))
			return Optional.of(NbtCompound_getUuid.get().invoke(this, key));
		return Optional.empty();
	}
	
	private static final Supplier<Reflection.MethodInvoker> NbtCompound_putUuid =
			Reflection.getOptionalMethod(NbtCompound.class, "method_25927", MethodType.methodType(void.class, String.class, UUID.class));
	public void nbte$putUuid(String key, UUID uuid) {
		if (NBT_CODE_REFACTORED)
			((NbtCompound) (Object) this).put(key, Uuids.INT_STREAM_CODEC, uuid);
		else
			NbtCompound_putUuid.get().invoke(this, key, uuid);
	}
	
}
