package com.luneruniverse.minecraft.mod.nbteditor.screens.nbtmenugenerators;

import java.util.List;
import java.util.function.Consumer;

import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTValue;
import com.luneruniverse.minecraft.mod.nbteditor.util.StringJsonWriterQuoted;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;

public class StringMenuGenerator implements MenuGenerator {
	
	@Override
	public List<NBTValue> getElements(NBTEditorScreen<?> screen, NbtElement source) {
		NbtElement nbt = getRealNbt(source);
		if (nbt == null)
			return null;
		MenuGenerator gen = MenuGenerator.TYPES.get(nbt.getType());
		if (gen == null || gen == this)
			return null;
		return gen.getElements(screen, nbt);
	}
	
	@Override
	public NbtElement getElement(NbtElement source, String key) {
		NbtElement nbt = getRealNbt(source);
		if (nbt == null)
			return null;
		MenuGenerator gen = MenuGenerator.TYPES.get(nbt.getType());
		if (gen == null || gen == this)
			return null;
		return gen.getElement(nbt, key);
	}
	
	@Override
	public void setElement(NbtElement source, String key, NbtElement value) {
		NbtElement nbt = getRealNbt(source);
		if (nbt == null)
			return;
		MenuGenerator gen = MenuGenerator.TYPES.get(nbt.getType());
		if (gen == null || gen == this)
			return;
		gen.setElement(nbt, key, value);
		save(source, nbt);
	}
	
	@Override
	public void addElement(NBTEditorScreen<?> screen, NbtElement source, Consumer<String> requestOverwrite, String force) {
		NbtElement nbt = getRealNbt(source);
		if (nbt == null)
			return;
		MenuGenerator gen = MenuGenerator.TYPES.get(nbt.getType());
		if (gen == null || gen == this)
			return;
		gen.addElement(screen, nbt, force2 -> {
			if (force2 == null)
				save(source, nbt);
			requestOverwrite.accept(force2);
		}, force);
	}
	
	@Override
	public void removeElement(NbtElement source, String key) {
		NbtElement nbt = getRealNbt(source);
		if (nbt == null)
			return;
		MenuGenerator gen = MenuGenerator.TYPES.get(nbt.getType());
		if (gen == null || gen == this)
			return;
		gen.removeElement(nbt, key);
		save(source, nbt);
	}
	
	@Override
	public void pasteElement(NbtElement source, String key, NbtElement value) {
		NbtElement nbt = getRealNbt(source);
		if (nbt == null)
			return;
		MenuGenerator gen = MenuGenerator.TYPES.get(nbt.getType());
		if (gen == null || gen == this)
			return;
		gen.pasteElement(nbt, key, value);
		save(source, nbt);
	}
	
	public boolean renameElement(NbtElement source, String key, String newKey, boolean force) {
		NbtElement nbt = getRealNbt(source);
		if (nbt == null)
			return true;
		MenuGenerator gen = MenuGenerator.TYPES.get(nbt.getType());
		if (gen == null || gen == this)
			return true;
		boolean output = gen.renameElement(nbt, key, newKey, force);
		save(source, nbt);
		return output;
	}
	
	private NbtElement getRealNbt(NbtElement str) {
		try {
			return MixinLink.parseSpecialElement(new StringReader(((NbtString) str).asString()));
		} catch (CommandSyntaxException e) {
			return null;
		}
	}
	
	private void save(NbtElement source, NbtElement nbt) {
		((NbtString) source).value = new StringJsonWriterQuoted().apply(nbt);
	}
	
}