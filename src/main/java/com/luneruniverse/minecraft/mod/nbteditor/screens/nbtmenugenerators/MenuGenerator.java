package com.luneruniverse.minecraft.mod.nbteditor.screens.nbtmenugenerators;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTValue;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.NbtElement;

public interface MenuGenerator {
	public static Map<Integer, MenuGenerator> TYPES = getGenerators();
	static Map<Integer, MenuGenerator> getGenerators() {
		Map<Integer, MenuGenerator> output = new HashMap<>();
		
		output.put(NbtType.COMPOUND, new CompoundMenuGenerator());
		
		MenuGenerator listMenuGeneratorSwitch = new ListMenuGeneratorSwitch();
		output.put(NbtType.LIST, listMenuGeneratorSwitch);
		output.put(NbtType.BYTE_ARRAY, listMenuGeneratorSwitch);
		output.put(NbtType.INT_ARRAY, listMenuGeneratorSwitch);
		output.put(NbtType.LONG_ARRAY, listMenuGeneratorSwitch);
		
		output.put(NbtType.STRING, new StringMenuGenerator());
		
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