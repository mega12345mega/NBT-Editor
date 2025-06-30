package com.luneruniverse.minecraft.mod.nbteditor.screens.nbtfolder;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTValue;
import com.luneruniverse.minecraft.mod.nbteditor.util.ClassMap;

import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;

public interface NBTFolder<T extends NbtElement> {
	
	public interface Constructor<T extends NbtElement> {
		public NBTFolder<T> create(Supplier<T> get, Consumer<T> set);
	}
	public static final ClassMap<NbtElement, Constructor<?>> TYPES = getTypesMap();
	private static ClassMap<NbtElement, Constructor<?>> getTypesMap() {
		ClassMap<NbtElement, Constructor<?>> output = new ClassMap<>();
		output.put(AbstractNbtList.class, (Constructor<AbstractNbtList<?>>) ListNBTFolder::new);
		output.put(NbtCompound.class, (Constructor<NbtCompound>) CompoundNBTFolder::new);
		output.put(NbtString.class, (Constructor<NbtString>) StringNBTFolder::new);
		return output;
	}
	@SuppressWarnings("unchecked")
	public static <T extends NbtElement> NBTFolder<T> get(Class<T> nbt, Supplier<T> get, Consumer<T> set) {
		Constructor<?> constructor = TYPES.get(nbt);
		if (constructor == null)
			return null;
		return ((Constructor<T>) constructor).create(get, set);
	}
	@SuppressWarnings("unchecked")
	public static <T extends NbtElement> NBTFolder<? extends T> get(T nbt) {
		AtomicReference<T> ref = new AtomicReference<>(nbt);
		return get((Class<T>) nbt.getClass(), ref::getPlain, ref::setPlain);
	}
	
	public T getNBT();
	public void setNBT(T value);
	
	public List<NBTValue> getEntries(NBTEditorScreen<?> screen);
	public boolean hasEmptyKey();
	
	public NbtElement getValue(String key);
	public void setValue(String key, NbtElement value);
	
	public void addKey(String key);
	public void removeKey(String key);
	
	public Optional<String> getNextKey(Optional<String> pastingKey);
	public Predicate<String> getKeyValidator(boolean renaming);
	public boolean handlesDuplicateKeys();
	
	public default NBTFolder<?> getSubFolder(String key) {
		NbtElement value = getValue(key);
		if (value == null)
			return null;
		return getSubFolder(key, value.getClass());
	}
	private <T2 extends NbtElement> NBTFolder<T2> getSubFolder(String key, Class<T2> clazz) {
		return get(clazz, () -> clazz.cast(getValue(key)), newValue -> setValue(key, newValue));
	}
	
}
