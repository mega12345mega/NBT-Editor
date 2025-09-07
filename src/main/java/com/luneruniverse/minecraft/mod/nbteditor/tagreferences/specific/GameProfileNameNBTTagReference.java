package com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific;

import java.util.Optional;

import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.TagReference;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class GameProfileNameNBTTagReference implements TagReference<Optional<String>, NbtCompound> {
	
	@Override
	public Optional<String> get(NbtCompound object) {
		if (object.nbte$contains("SkullOwner", NbtElement.STRING_TYPE))
			return Optional.of(object.nbte$getStringOrDefault("SkullOwner"));
		if (object.nbte$contains("SkullOwner", NbtElement.COMPOUND_TYPE)) {
			NbtCompound skullOwner = object.nbte$getCompoundOrDefault("SkullOwner");
			if (skullOwner.nbte$contains("Name", NbtElement.STRING_TYPE))
				return Optional.of(skullOwner.nbte$getStringOrDefault("Name"));
			return Optional.empty();
		}
		return Optional.empty();
	}
	
	@Override
	public void set(NbtCompound object, Optional<String> value) {
		value.ifPresentOrElse(name -> object.putString("SkullOwner", name), () -> object.remove("SkullOwner"));
	}
	
}
