package com.luneruniverse.minecraft.mod.nbteditor.screens.nbtmenugenerators;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTValue;

import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtShort;
import net.minecraft.nbt.NbtString;

public class ListMenuGenerator<T extends NbtElement, L extends AbstractNbtList<? extends NbtElement>> implements MenuGenerator {
	
	private final T defaultValue;
	public ListMenuGenerator(T defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<NBTValue> getElements(NBTEditorScreen<?> screen, NbtElement source) {
		AbstractNbtList<? extends NbtElement> nbt = (AbstractNbtList<T>) source;
		List<NBTValue> output = new ArrayList<>();
		for (int i = 0; i < nbt.size(); i++)
			output.add(new NBTValue(screen, i + "", nbt.get(i), nbt));
		return output;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public NbtElement getElement(NbtElement source, String key) {
		try {
			return ((AbstractNbtList<T>) source).get(Integer.parseInt(key));
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setElement(NbtElement source, String key, NbtElement value) {
		try {
			AbstractNbtList<T> list = (AbstractNbtList<T>) source;
			int index = Integer.parseInt(key);
			if (list.size() == 1 && index == 0 && list instanceof NbtList) {
				NbtList nonGenericList = (NbtList) list;
				nonGenericList.remove(0);
				nonGenericList.add(value);
			} else if (list.getHeldType() == value.getType())
				list.set(index, (T) value);
		} catch (NumberFormatException e) {
			NBTEditor.LOGGER.error("Error while modifying a list", e);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void addElement(NBTEditorScreen<?> screen, NbtElement source, Consumer<String> requestOverwrite, String force) {
		((AbstractNbtList<T>) source).add((T) defaultValue.copy());
		requestOverwrite.accept(null);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void removeElement(NbtElement source, String key) {
		try {
			((AbstractNbtList<T>) source).remove(Integer.parseInt(key));
		} catch (NumberFormatException e) {
			NBTEditor.LOGGER.error("Error while modifying a list", e);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void pasteElement(NbtElement source, String key, NbtElement value) {
		AbstractNbtList<T> list = (AbstractNbtList<T>) source;
		if (list.getHeldType() == 0 || list.getHeldType() == value.getType())
			list.add((T) value);
		else if (list.getHeldType() == NbtElement.STRING_TYPE)
			list.add((T) NbtString.of(value.toString()));
		else if (value instanceof AbstractNbtNumber) {
			AbstractNbtNumber num = (AbstractNbtNumber) value;
			
			switch (list.getHeldType()) {
				case NbtElement.BYTE_TYPE:
					list.add((T) NbtByte.of(num.byteValue()));
					break;
				case NbtElement.SHORT_TYPE:
					list.add((T) NbtShort.of(num.shortValue()));
					break;
				case NbtElement.INT_TYPE:
					list.add((T) NbtInt.of(num.intValue()));
					break;
				case NbtElement.LONG_TYPE:
					list.add((T) NbtLong.of(num.longValue()));
					break;
				case NbtElement.FLOAT_TYPE:
					list.add((T) NbtFloat.of(num.floatValue()));
					break;
				case NbtElement.DOUBLE_TYPE:
					list.add((T) NbtDouble.of(num.doubleValue()));
					break;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean renameElement(NbtElement source, String key, String newKey, boolean force) {
		AbstractNbtList<T> list = (AbstractNbtList<T>) source;
		try {
			NbtElement value = getElement(source, key);
			int keyInt = Integer.parseInt(key);
			int newKeyInt = Integer.parseInt(newKey);
			if (newKeyInt < 0)
				throw new NumberFormatException(newKeyInt + " is less than 0!");
			
			if (newKeyInt >= list.size()) {
				list.remove(keyInt);
				list.add((T) value);
			} else {
				list.remove(keyInt);
				list.add(newKeyInt, (T) value);
			}
			
			return true;
		} catch (NumberFormatException e) {
			NBTEditor.LOGGER.error("Error while modifying a list", e);
			return true;
		}
	}
	
}