package com.luneruniverse.minecraft.mod.nbteditor.screens.nbtmenugenerators;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTValue;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class CompoundMenuGenerator implements MenuGenerator<NbtCompound> {
	
	public static final CompoundMenuGenerator INSTANCE = new CompoundMenuGenerator();
	
	private CompoundMenuGenerator() {}
	
	@Override
	public List<NBTValue> getEntries(NbtCompound nbt, NBTEditorScreen<?> screen) {
		return nbt.getKeys().stream().map(key -> new NBTValue(screen, key, nbt.get(key))).collect(Collectors.toList());
	}
	
	@Override
	public boolean hasEmptyKey(NbtCompound nbt) {
		return nbt.contains("");
	}
	
	@Override
	public NbtElement getValue(NbtCompound nbt, String key) {
		return nbt.get(key);
	}
	
	@Override
	public void setValue(NbtCompound nbt, String key, NbtElement value) {
		nbt.put(key, value);
	}
	
	@Override
	public void addKey(NbtCompound nbt, String key) {
		nbt.putInt(key, 0);
	}
	
	@Override
	public void removeKey(NbtCompound nbt, String key) {
		nbt.remove(key);
	}
	
	@Override
	public Optional<String> getNextKey(NbtCompound nbt, Optional<String> pastingKey) {
		return pastingKey.map(key -> {
			if (nbt.contains(key)) {
				key += " - Copy";
				String baseKey = key;
				for (int i = 2; nbt.contains(key); i++)
					key = baseKey + " (" + i + ")";
			}
			return key;
		});
	}
	
	@Override
	public Predicate<String> getKeyValidator(NbtCompound nbt, boolean renaming) {
		return key -> true;
	}
	
	@Override
	public boolean handlesDuplicateKeys(NbtCompound nbt) {
		return false;
	}
	
}
