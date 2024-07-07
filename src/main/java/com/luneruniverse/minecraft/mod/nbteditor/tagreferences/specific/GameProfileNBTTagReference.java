package com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific;

import java.lang.invoke.MethodType;
import java.util.Optional;
import java.util.UUID;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.TagReference;
import com.mojang.authlib.GameProfile;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class GameProfileNBTTagReference implements TagReference<Optional<GameProfile>, NbtCompound> {
	
	private static final Class<?> NbtHelper = Reflection.getClass("net.minecraft.class_2512");
	
	private static final Reflection.MethodInvoker NbtHelper_toGameProfile =
			Reflection.getMethod(NbtHelper, "method_10683", MethodType.methodType(GameProfile.class, NbtCompound.class));
	@Override
	public Optional<GameProfile> get(NbtCompound object) {
		if (object.contains("SkullOwner", NbtElement.STRING_TYPE))
			return Optional.of(new GameProfile(new UUID(0L, 0L), object.getString("SkullOwner")));
		if (object.contains("SkullOwner", NbtElement.COMPOUND_TYPE))
			return Optional.ofNullable(NbtHelper_toGameProfile.invoke(null, object.getCompound("SkullOwner")));
		return Optional.empty();
	}
	
	private static final Reflection.MethodInvoker NbtHelper_writeGameProfile =
			Reflection.getMethod(NbtHelper, "method_10684", MethodType.methodType(NbtCompound.class, NbtCompound.class, GameProfile.class));
	@Override
	public void set(NbtCompound object, Optional<GameProfile> value) {
		value.ifPresentOrElse(
				profile -> object.put("SkullOwner", NbtHelper_writeGameProfile.invoke(null, new NbtCompound(), value.get())),
				() -> object.remove("SkullOwner"));
	}
	
}
