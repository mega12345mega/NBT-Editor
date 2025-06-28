package com.luneruniverse.minecraft.mod.nbteditor.screens.nbtmenugenerators;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTValue;

import net.minecraft.nbt.NbtElement;

public interface MenuGenerator<T extends NbtElement> {
	public static final Map<Byte, MenuGenerator<?>> TYPES = Map.of(
			NbtElement.BYTE_ARRAY_TYPE, ListMenuGenerator.INSTANCE,
			NbtElement.INT_ARRAY_TYPE, ListMenuGenerator.INSTANCE,
			NbtElement.LONG_ARRAY_TYPE, ListMenuGenerator.INSTANCE,
			NbtElement.LIST_TYPE, ListMenuGenerator.INSTANCE,
			NbtElement.COMPOUND_TYPE, CompoundMenuGenerator.INSTANCE,
			NbtElement.STRING_TYPE, StringMenuGenerator.INSTANCE);
	@SuppressWarnings("unchecked")
	public static <T extends NbtElement> MenuGenerator<T> get(T nbt) {
		return (MenuGenerator<T>) TYPES.get(nbt.getType());
	}
	
	public List<NBTValue> getEntries(T nbt, NBTEditorScreen<?> screen);
	public boolean hasEmptyKey(T nbt);
	public NbtElement getValue(T nbt, String key);
	public void setValue(T nbt, String key, NbtElement value);
	public void addKey(T nbt, String key);
	public void removeKey(T nbt, String key);
	public Optional<String> getNextKey(T nbt, Optional<String> pastingKey);
	public Predicate<String> getKeyValidator(T nbt, boolean renaming);
	public boolean handlesDuplicateKeys(T nbt);
}
