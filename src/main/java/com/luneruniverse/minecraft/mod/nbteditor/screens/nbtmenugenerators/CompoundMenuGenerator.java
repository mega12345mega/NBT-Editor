package com.luneruniverse.minecraft.mod.nbteditor.screens.nbtmenugenerators;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTValue;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;

public class CompoundMenuGenerator implements MenuGenerator {
	
	@Override
	public List<NBTValue> getElements(NBTEditorScreen<?> screen, NbtElement source) {
		NbtCompound nbt = (NbtCompound) source;
		return nbt.getKeys().stream().map(key -> new NBTValue(screen, key, nbt.get(key))).collect(Collectors.toList());
	}
	
	@Override
	public NbtElement getElement(NbtElement source, String key) {
		return ((NbtCompound) source).get(key);
	}
	
	@Override
	public void setElement(NbtElement source, String key, NbtElement value) {
		((NbtCompound) source).put(key, value);
	}
	
	@Override
	public void addElement(NBTEditorScreen<?> screen, NbtElement source, Consumer<String> requestOverwrite, String force) {
		Consumer<String> main = key -> {
			NbtCompound nbt = (NbtCompound) source;
			if (nbt.contains(key) && force == null)
				requestOverwrite.accept(key);
			else {
				nbt.put(key, NbtInt.of(0));
				requestOverwrite.accept(null);
			}
		};
		if (force == null)
			screen.getKey(main);
		else
			main.accept(force);
	}
	
	@Override
	public void removeElement(NbtElement source, String key) {
		((NbtCompound) source).remove(key);
	}
	
	@Override
	public void pasteElement(NbtElement source, String key, NbtElement value) {
		NbtCompound nbt = (NbtCompound) source;
		if (nbt.contains(key)) {
			if (nbt.contains(key + " - Copy")) {
				int i = 2;
				while (nbt.contains(key + " - Copy (" + i + ")"))
					i++;
				key += " - Copy (" + i + ")";
			} else
				key += " - Copy";
		}
		nbt.put(key, value);
	}
	
	@Override
	public boolean renameElement(NbtElement source, String key, String newKey, boolean force) {
		NbtCompound nbt = (NbtCompound) source;
		if (nbt.contains(newKey) && !force)
			return false;
		NbtElement value = nbt.get(key);
		nbt.remove(key);
		nbt.put(newKey, value);
		return true;
	}
	
}