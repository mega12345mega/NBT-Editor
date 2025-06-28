package com.luneruniverse.minecraft.mod.nbteditor.screens.nbtmenugenerators;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTValue;
import com.luneruniverse.minecraft.mod.nbteditor.util.StringJsonWriterQuoted;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;

public class StringMenuGenerator implements MenuGenerator<NbtString> {
	
	public static final StringMenuGenerator INSTANCE = new StringMenuGenerator();
	
	private StringMenuGenerator() {}
	
	@Override
	public List<NBTValue> getEntries(NbtString nbt, NBTEditorScreen<?> screen) {
		return exec(nbt, (gen, parsedNbt) -> gen.getEntries(parsedNbt, screen), null, false);
	}
	
	@Override
	public boolean hasEmptyKey(NbtString nbt) {
		return exec(nbt, MenuGenerator::hasEmptyKey, false, false);
	}
	
	@Override
	public NbtElement getValue(NbtString nbt, String key) {
		return exec(nbt, (gen, parsedNbt) -> gen.getValue(parsedNbt, key), null, false);
	}
	
	@Override
	public void setValue(NbtString nbt, String key, NbtElement value) {
		execVoid(nbt, (gen, parsedNbt) -> gen.setValue(parsedNbt, key, value), true);
	}
	
	@Override
	public void addKey(NbtString nbt, String key) {
		execVoid(nbt, (gen, parsedNbt) -> gen.addKey(parsedNbt, key), true);
	}
	
	@Override
	public void removeKey(NbtString nbt, String key) {
		execVoid(nbt, (gen, parsedNbt) -> gen.removeKey(parsedNbt, key), true);
	}
	
	@Override
	public Optional<String> getNextKey(NbtString nbt, Optional<String> pastingKey) {
		return exec(nbt, (gen, parsedNbt) -> gen.getNextKey(parsedNbt, pastingKey), Optional.empty(), false);
	}
	
	@Override
	public Predicate<String> getKeyValidator(NbtString nbt, boolean renaming) {
		return exec(nbt, (gen, parsedNbt) -> gen.getKeyValidator(parsedNbt, renaming), key -> false, false);
	}
	
	@Override
	public boolean handlesDuplicateKeys(NbtString nbt) {
		return exec(nbt, MenuGenerator::handlesDuplicateKeys, false, false);
	}
	
	private NbtElement parseNbt(NbtString nbt) {
		try {
			return MixinLink.parseSpecialElement(new StringReader(nbt.asString()));
		} catch (CommandSyntaxException e) {
			return null;
		}
	}
	private void saveNbt(NbtString nbt, NbtElement parsedNbt) {
		nbt.value = new StringJsonWriterQuoted().apply(parsedNbt);
	}
	
	private <R> R exec(NbtString nbt, BiFunction<MenuGenerator<NbtElement>, NbtElement, R> executor, R defaultReturnValue, boolean save) {
		NbtElement parsedNbt = parseNbt(nbt);
		if (parsedNbt == null || nbt.equals(parsedNbt))
			return defaultReturnValue;
		MenuGenerator<NbtElement> gen = MenuGenerator.get(parsedNbt);
		if (gen == null)
			return defaultReturnValue;
		
		R output = executor.apply(gen, parsedNbt);
		if (save)
			saveNbt(nbt, parsedNbt);
		return output;
	}
	private boolean execVoid(NbtString nbt, BiConsumer<MenuGenerator<NbtElement>, NbtElement> executor, boolean save) {
		return exec(nbt, (gen, parsedNbt) -> {
			executor.accept(gen, parsedNbt);
			return true;
		}, false, save);
	}
	
}
