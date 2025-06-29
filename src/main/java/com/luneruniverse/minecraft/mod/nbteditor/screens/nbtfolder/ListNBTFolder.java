package com.luneruniverse.minecraft.mod.nbteditor.screens.nbtfolder;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTValue;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtShort;
import net.minecraft.nbt.NbtString;

public class ListNBTFolder implements NBTFolder<AbstractNbtList<?>> {
	
	private final Supplier<AbstractNbtList<?>> get;
	private final Consumer<AbstractNbtList<?>> set;
	
	public ListNBTFolder(Supplier<AbstractNbtList<?>> get, Consumer<AbstractNbtList<?>> set) {
		this.get = get;
		this.set = set;
	}
	
	@Override
	public AbstractNbtList<?> getNBT() {
		return get.get();
	}
	
	@Override
	public void setNBT(AbstractNbtList<?> value) {
		set.accept(value);
	}
	
	@Override
	public List<NBTValue> getEntries(NBTEditorScreen<?> screen) {
		AbstractNbtList<?> nbt = getNBT();
		return IntStream.range(0, nbt.size())
				.mapToObj(i -> new NBTValue(screen, i + "", nbt.get(i), nbt)).collect(Collectors.toList());
	}
	
	@Override
	public boolean hasEmptyKey() {
		return false;
	}
	
	@Override
	public NbtElement getValue(String key) {
		AbstractNbtList<?> nbt = getNBT();
		try {
			int i = Integer.parseInt(key);
			if (i < 0 || i >= nbt.size())
				return null;
			return nbt.get(i);
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	@Override
	public void setValue(String key, NbtElement value) {
		AbstractNbtList<?> nbt = getNBT();
		int i = Integer.parseInt(key);
		if (nbt.size() == 1 && i == 0 && nbt instanceof NbtList list) {
			list.remove(0);
			list.add(value);
			setNBT(nbt);
		} else
			setValue(nbt, i, value);
	}
	private <C extends NbtElement> void setValue(AbstractNbtList<C> nbt, int i, NbtElement value) {
		C convertedValue = convertToType(nbt, value);
		if (convertedValue != null) {
			nbt.set(i, convertedValue);
			setNBT(nbt);
		}
	}
	
	@Override
	public void addKey(String key) {
		AbstractNbtList<?> nbt = getNBT();
		addKey(nbt, Integer.parseInt(key));
		setNBT(nbt);
	}
	private <C extends NbtElement> void addKey(AbstractNbtList<C> nbt, int i) {
		nbt.add(i, getDefaultValue(nbt));
	}
	
	@Override
	public void removeKey(String key) {
		AbstractNbtList<?> nbt = getNBT();
		try {
			int i = Integer.parseInt(key);
			if (i >= 0 && i < nbt.size()) {
				nbt.remove(i);
				setNBT(nbt);
			}
		} catch (NumberFormatException e) {}
	}
	
	@Override
	public Optional<String> getNextKey(Optional<String> pastingKey) {
		return Optional.of(getNBT().size() + "");
	}
	
	@SuppressWarnings("unchecked")
	private <C extends NbtElement> C convertToType(AbstractNbtList<C> nbt, NbtElement value) {
		int heldType = nbt.getHeldType();
		
		if (heldType == 0 || heldType == value.getType())
			return (C) value;
		
		if (heldType == NbtElement.COMPOUND_TYPE) {
			NbtCompound output = new NbtCompound();
			output.put("value", value);
			return (C) output;
		}
		if (heldType == NbtElement.LIST_TYPE) {
			NbtList output = new NbtList();
			output.add(value);
			return (C) output;
		}
		if (heldType == NbtElement.STRING_TYPE)
			return (C) NbtString.of(value.toString());
		
		if (value instanceof AbstractNbtNumber num) {
			return switch (heldType) {
				case NbtElement.BYTE_TYPE -> (C) NbtByte.of(num.byteValue());
				case NbtElement.SHORT_TYPE -> (C) NbtShort.of(num.shortValue());
				case NbtElement.INT_TYPE -> (C) NbtInt.of(num.intValue());
				case NbtElement.LONG_TYPE -> (C) NbtLong.of(num.longValue());
				case NbtElement.FLOAT_TYPE -> (C) NbtFloat.of(num.floatValue());
				case NbtElement.DOUBLE_TYPE -> (C) NbtDouble.of(num.doubleValue());
				case NbtElement.BYTE_ARRAY_TYPE -> (C) new NbtByteArray(new byte[] {num.byteValue()});
				case NbtElement.INT_ARRAY_TYPE -> (C) new NbtIntArray(new int[] {num.intValue()});
				case NbtElement.LONG_ARRAY_TYPE -> (C) new NbtLongArray(new long[] {num.longValue()});
				default -> null;
			};
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private <C extends NbtElement> C getDefaultValue(AbstractNbtList<C> nbt) {
		return switch (nbt.getHeldType()) {
			case NbtElement.BYTE_TYPE -> (C) NbtByte.ZERO;
			case NbtElement.SHORT_TYPE -> (C) NbtShort.of((short) 0);
			case 0, NbtElement.INT_TYPE -> (C) NbtInt.of(0);
			case NbtElement.LONG_TYPE -> (C) NbtLong.of(0);
			case NbtElement.FLOAT_TYPE -> (C) NbtFloat.ZERO;
			case NbtElement.DOUBLE_TYPE -> (C) NbtDouble.ZERO;
			case NbtElement.BYTE_ARRAY_TYPE -> (C) new NbtByteArray(new byte[0]);
			case NbtElement.INT_ARRAY_TYPE -> (C) new NbtIntArray(new int[0]);
			case NbtElement.LONG_ARRAY_TYPE -> (C) new NbtLongArray(new long[0]);
			case NbtElement.LIST_TYPE -> (C) new NbtList();
			case NbtElement.COMPOUND_TYPE -> (C) new NbtCompound();
			case NbtElement.STRING_TYPE -> (C) NbtString.of("");
			default -> throw new IllegalArgumentException("Unknown NBT type: " + nbt.getHeldType());
		};
	}
	
	@Override
	public Predicate<String> getKeyValidator(boolean renaming) {
		return MainUtil.intPredicate(() -> 0, () -> getNBT().size() + (renaming ? -1 : 0), false);
	}
	
	@Override
	public boolean handlesDuplicateKeys() {
		return true;
	}
	
}
