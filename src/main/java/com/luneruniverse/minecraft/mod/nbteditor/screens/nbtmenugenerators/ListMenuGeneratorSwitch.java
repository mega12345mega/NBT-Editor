package com.luneruniverse.minecraft.mod.nbteditor.screens.nbtmenugenerators;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTValue;

import net.minecraft.nbt.AbstractNbtList;
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

public class ListMenuGeneratorSwitch implements MenuGenerator {
	
	private static final Map<Byte, MenuGenerator> listMenuGenerators;
	static {
		listMenuGenerators = new HashMap<>();
		
		listMenuGenerators.put(NbtElement.BYTE_TYPE, new ListMenuGenerator<NbtByte, NbtByteArray>(NbtByte.of((byte) 0)));
		listMenuGenerators.put(NbtElement.INT_TYPE, new ListMenuGenerator<NbtInt, NbtIntArray>(NbtInt.of(0)));
		listMenuGenerators.put(NbtElement.LONG_TYPE, new ListMenuGenerator<NbtLong, NbtLongArray>(NbtLong.of(0L)));
		
		listMenuGenerators.put(NbtElement.BYTE_ARRAY_TYPE, new ListMenuGenerator<NbtByteArray, NbtList>(new NbtByteArray(new byte[0])));
		listMenuGenerators.put(NbtElement.COMPOUND_TYPE, new ListMenuGenerator<NbtCompound, NbtList>(new NbtCompound()));
		listMenuGenerators.put(NbtElement.DOUBLE_TYPE, new ListMenuGenerator<NbtDouble, NbtList>(NbtDouble.of(0)));
		listMenuGenerators.put(NbtElement.FLOAT_TYPE, new ListMenuGenerator<NbtFloat, NbtList>(NbtFloat.of(0)));
		listMenuGenerators.put(NbtElement.INT_ARRAY_TYPE, new ListMenuGenerator<NbtIntArray, NbtList>(new NbtIntArray(new int[0])));
		listMenuGenerators.put(NbtElement.LIST_TYPE, new ListMenuGenerator<NbtList, NbtList>(new NbtList()));
		listMenuGenerators.put(NbtElement.LONG_ARRAY_TYPE, new ListMenuGenerator<NbtLongArray, NbtList>(new NbtLongArray(new long[0])));
		listMenuGenerators.put(NbtElement.SHORT_TYPE, new ListMenuGenerator<NbtShort, NbtList>(NbtShort.of((short) 0)));
		listMenuGenerators.put(NbtElement.STRING_TYPE, new ListMenuGenerator<NbtString, NbtList>(NbtString.of("")));
		
		listMenuGenerators.put((byte) 0, listMenuGenerators.get(NbtElement.INT_TYPE));
	}
	
	@Override
	public List<NBTValue> getElements(NBTEditorScreen screen, NbtElement source) {
		return getRealGen(source).getElements(screen, source);
	}
	
	@Override
	public NbtElement getElement(NbtElement source, String key) {
		return getRealGen(source).getElement(source, key);
	}
	
	@Override
	public void setElement(NbtElement source, String key, NbtElement value) {
		getRealGen(source).setElement(source, key, value);
	}
	
	@Override
	public void addElement(NBTEditorScreen screen, NbtElement source, Consumer<String> requestOverwrite, String force) {
		getRealGen(source).addElement(screen, source, requestOverwrite, force);
	}
	
	@Override
	public void removeElement(NbtElement source, String key) {
		getRealGen(source).removeElement(source, key);
	}
	
	@Override
	public void pasteElement(NbtElement source, String key, NbtElement value) {
		getRealGen(source).pasteElement(source, key, value);
	}
	
	@Override
	public boolean renameElement(NbtElement source, String key, String newKey, boolean force) {
		return getRealGen(source).renameElement(source, key, newKey, force);
	}
	
	@SuppressWarnings("unchecked")
	private MenuGenerator getRealGen(NbtElement source) {
		return listMenuGenerators.get(((AbstractNbtList<? extends NbtElement>) source).getHeldType());
	}
	
}