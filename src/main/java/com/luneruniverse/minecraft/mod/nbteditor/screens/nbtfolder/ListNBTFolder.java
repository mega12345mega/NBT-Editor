package com.luneruniverse.minecraft.mod.nbteditor.screens.nbtfolder;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.MVNbtCompoundParent;
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

public class ListNBTFolder implements NBTFolder<AbstractNbtList> {
	
	private final Supplier<AbstractNbtList> get;
	private final Consumer<AbstractNbtList> set;
	
	public ListNBTFolder(Supplier<AbstractNbtList> get, Consumer<AbstractNbtList> set) {
		this.get = get;
		this.set = set;
	}
	
	@Override
	public AbstractNbtList getNBT() {
		return get.get();
	}
	
	@Override
	public void setNBT(AbstractNbtList value) {
		set.accept(value);
	}
	
	@Override
	public List<NBTValue> getEntries(NBTEditorScreen<?> screen) {
		AbstractNbtList nbt = getNBT();
		return IntStream.range(0, nbt.nbte$size())
				.mapToObj(i -> new NBTValue(screen, i + "", nbt.nbte$get(i), nbt)).collect(Collectors.toList());
	}
	
	@Override
	public boolean hasEmptyKey() {
		return false;
	}
	
	@Override
	public NbtElement getValue(String key) {
		AbstractNbtList nbt = getNBT();
		try {
			int i = Integer.parseInt(key);
			if (i < 0 || i >= nbt.nbte$size())
				return null;
			return nbt.nbte$get(i);
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	@Override
	public void setValue(String key, NbtElement value) {
		AbstractNbtList nbt = getNBT();
		int i = Integer.parseInt(key);
		if (nbt.nbte$size() == 1 && i == 0 && nbt instanceof NbtList list) {
			if (MVNbtCompoundParent.NBT_CODE_REFACTORED) {
				list.setElement(0, value);
			} else {
				list.remove(0);
				list.add(value);
			}
			setNBT(nbt);
		} else {
			NbtElement convertedValue = convertToType(nbt, value);
			if (convertedValue != null) {
				nbt.nbte$set(i, convertedValue);
				setNBT(nbt);
			}
		}
	}
	
	@Override
	public void addKey(String key) {
		AbstractNbtList nbt = getNBT();
		nbt.nbte$add(Integer.parseInt(key), getDefaultValue(nbt));
		setNBT(nbt);
	}
	
	@Override
	public void removeKey(String key) {
		AbstractNbtList nbt = getNBT();
		try {
			int i = Integer.parseInt(key);
			if (i >= 0 && i < nbt.nbte$size()) {
				nbt.nbte$remove(i);
				setNBT(nbt);
			}
		} catch (NumberFormatException e) {}
	}
	
	@Override
	public Optional<String> getNextKey(Optional<String> pastingKey) {
		return Optional.of(getNBT().nbte$size() + "");
	}
	
	private NbtElement convertToType(AbstractNbtList nbt, NbtElement value) {
		if (MVNbtCompoundParent.NBT_CODE_REFACTORED)
			return value;
		
		int heldType = nbt.nbte$getHeldType().get();
		
		if (heldType == 0 || heldType == value.getType())
			return value;
		
		if (heldType == NbtElement.COMPOUND_TYPE) {
			NbtCompound output = new NbtCompound();
			output.put("value", value);
			return output;
		}
		if (heldType == NbtElement.LIST_TYPE) {
			NbtList output = new NbtList();
			output.add(value);
			return output;
		}
		if (heldType == NbtElement.STRING_TYPE)
			return NbtString.of(value.toString());
		
		if (value instanceof AbstractNbtNumber num) {
			return switch (heldType) {
				case NbtElement.BYTE_TYPE -> NbtByte.of(num.nbte$byteValue());
				case NbtElement.SHORT_TYPE -> NbtShort.of(num.nbte$shortValue());
				case NbtElement.INT_TYPE -> NbtInt.of(num.nbte$intValue());
				case NbtElement.LONG_TYPE -> NbtLong.of(num.nbte$longValue());
				case NbtElement.FLOAT_TYPE -> NbtFloat.of(num.nbte$floatValue());
				case NbtElement.DOUBLE_TYPE -> NbtDouble.of(num.nbte$doubleValue());
				case NbtElement.BYTE_ARRAY_TYPE -> new NbtByteArray(new byte[] {num.nbte$byteValue()});
				case NbtElement.INT_ARRAY_TYPE -> new NbtIntArray(new int[] {num.nbte$intValue()});
				case NbtElement.LONG_ARRAY_TYPE -> new NbtLongArray(new long[] {num.nbte$longValue()});
				default -> null;
			};
		}
		
		return null;
	}
	
	private NbtElement getDefaultValue(AbstractNbtList nbt) {
		return switch (nbt.nbte$getHeldType().orElse((byte) 0)) {
			case NbtElement.BYTE_TYPE -> NbtByte.ZERO;
			case NbtElement.SHORT_TYPE -> NbtShort.of((short) 0);
			case 0, NbtElement.INT_TYPE -> NbtInt.of(0);
			case NbtElement.LONG_TYPE -> NbtLong.of(0);
			case NbtElement.FLOAT_TYPE -> NbtFloat.ZERO;
			case NbtElement.DOUBLE_TYPE -> NbtDouble.ZERO;
			case NbtElement.BYTE_ARRAY_TYPE -> new NbtByteArray(new byte[0]);
			case NbtElement.INT_ARRAY_TYPE -> new NbtIntArray(new int[0]);
			case NbtElement.LONG_ARRAY_TYPE -> new NbtLongArray(new long[0]);
			case NbtElement.LIST_TYPE -> new NbtList();
			case NbtElement.COMPOUND_TYPE -> new NbtCompound();
			case NbtElement.STRING_TYPE -> NbtString.of("");
			default -> throw new IllegalArgumentException("Unknown NBT type: " + nbt.nbte$getHeldType().get());
		};
	}
	
	@Override
	public Predicate<String> getKeyValidator(boolean renaming) {
		return MainUtil.intPredicate(() -> 0, () -> getNBT().nbte$size() + (renaming ? -1 : 0), false);
	}
	
	@Override
	public boolean handlesDuplicateKeys() {
		return true;
	}
	
}
