package com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific;

import java.util.Optional;

import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.TagReference;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class GameProfileNameNBTTagReference implements TagReference<Optional<String>, NbtCompound> {
	
	@Override
	public Optional<String> get(NbtCompound object) {
		if (object.contains("SkullOwner", NbtElement.STRING_TYPE))
			return Optional.of(object.getString("SkullOwner"));
		if (object.contains("SkullOwner", NbtElement.COMPOUND_TYPE)) {
			NbtCompound skullOwner = object.getCompound("SkullOwner");
			if (skullOwner.contains("Name", NbtElement.STRING_TYPE))
				return Optional.of(skullOwner.getString("Name"));
			return Optional.empty();
		}
		return Optional.empty();
	}
	
	@Override
	public void set(NbtCompound object, Optional<String> value) {
		value.ifPresentOrElse(name -> object.putString("SkullOwner", name), () -> object.remove("SkullOwner"));
	}
	
}
