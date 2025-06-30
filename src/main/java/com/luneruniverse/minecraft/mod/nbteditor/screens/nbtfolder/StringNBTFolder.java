package com.luneruniverse.minecraft.mod.nbteditor.screens.nbtfolder;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTValue;
import com.luneruniverse.minecraft.mod.nbteditor.util.StringJsonWriterQuoted;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;

public class StringNBTFolder implements NBTFolder<NbtString> {
	
	private final Supplier<NbtString> get;
	private final Consumer<NbtString> set;
	
	public StringNBTFolder(Supplier<NbtString> get, Consumer<NbtString> set) {
		this.get = get;
		this.set = set;
	}
	
	@Override
	public NbtString getNBT() {
		return get.get();
	}
	
	@Override
	public void setNBT(NbtString value) {
		set.accept(value);
	}
	
	@Override
	public List<NBTValue> getEntries(NBTEditorScreen<?> screen) {
		return exec(folder -> folder.getEntries(screen), null, false);
	}
	
	@Override
	public boolean hasEmptyKey() {
		return exec(NBTFolder::hasEmptyKey, false, false);
	}
	
	@Override
	public NbtElement getValue(String key) {
		return exec(folder -> folder.getValue(key), null, false);
	}
	
	@Override
	public void setValue(String key, NbtElement value) {
		execVoid(folder -> folder.setValue(key, value), true);
	}
	
	@Override
	public void addKey(String key) {
		execVoid(folder -> folder.addKey(key), true);
	}
	
	@Override
	public void removeKey(String key) {
		execVoid(folder -> folder.removeKey(key), true);
	}
	
	@Override
	public Optional<String> getNextKey(Optional<String> pastingKey) {
		return exec(folder -> folder.getNextKey(pastingKey), Optional.empty(), false);
	}
	
	@Override
	public Predicate<String> getKeyValidator(boolean renaming) {
		return exec(folder -> folder.getKeyValidator(renaming), key -> false, false);
	}
	
	@Override
	public boolean handlesDuplicateKeys() {
		return exec(NBTFolder::handlesDuplicateKeys, false, false);
	}
	
	private <R> R exec(Function<NBTFolder<?>, R> executor, R defaultReturnValue, boolean save) {
		NbtElement parsedNbt;
		try {
			parsedNbt = MixinLink.parseSpecialElement(new StringReader(getNBT().asString()));
		} catch (CommandSyntaxException e) {
			return defaultReturnValue;
		}
		
		if (getNBT().equals(parsedNbt))
			return defaultReturnValue;
		
		NBTFolder<?> folder = NBTFolder.get(parsedNbt);
		if (folder == null)
			return defaultReturnValue;
		
		R output = executor.apply(folder);
		if (save)
			setNBT(NbtString.of(new StringJsonWriterQuoted().apply(folder.getNBT())));
		return output;
	}
	private boolean execVoid(Consumer<NBTFolder<?>> executor, boolean save) {
		return exec(folder -> {
			executor.accept(folder);
			return true;
		}, false, save);
	}
	
}
