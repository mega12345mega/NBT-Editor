package com.luneruniverse.minecraft.mod.nbteditor.screens.nbtmenugenerators;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTValue;

import net.minecraft.nbt.NbtElement;

public interface MenuGenerator {
	public static Map<Byte, MenuGenerator> TYPES = getGenerators();
	static Map<Byte, MenuGenerator> getGenerators() {
		Map<Byte, MenuGenerator> output = new HashMap<>();
		
		output.put(NbtElement.COMPOUND_TYPE, new CompoundMenuGenerator());
		
		MenuGenerator listMenuGeneratorSwitch = new ListMenuGeneratorSwitch();
		output.put(NbtElement.LIST_TYPE, listMenuGeneratorSwitch);
		output.put(NbtElement.BYTE_ARRAY_TYPE, listMenuGeneratorSwitch);
		output.put(NbtElement.INT_ARRAY_TYPE, listMenuGeneratorSwitch);
		output.put(NbtElement.LONG_ARRAY_TYPE, listMenuGeneratorSwitch);
		
		output.put(NbtElement.STRING_TYPE, new StringMenuGenerator());
		
		return output;
	}
	
	public List<NBTValue> getElements(NBTEditorScreen screen, NbtElement source);
	public NbtElement getElement(NbtElement source, String key);
	public default boolean hasEmptyKey(NBTEditorScreen screen, NbtElement source) {
		List<NBTValue> elements = getElements(screen, source);
		if (elements == null)
			return false;
		return elements.stream().anyMatch(value -> value.getKey().isEmpty());
	}
	public void setElement(NbtElement source, String key, NbtElement value);
	public void addElement(NBTEditorScreen screen, NbtElement source, Consumer<String> requestOverwrite, String force);
	public void removeElement(NbtElement source, String key);
	public void pasteElement(NbtElement source, String key, NbtElement value);
	public boolean renameElement(NbtElement source, String key, String newKey, boolean force);
}