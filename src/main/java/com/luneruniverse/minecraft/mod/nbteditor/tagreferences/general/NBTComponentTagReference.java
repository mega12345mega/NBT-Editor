package com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;

public class NBTComponentTagReference<T, C> implements TagReference<T, NbtCompound> {
	
	private final String tag;
	private final Codec<C> codec;
	private final Supplier<T> defaultValue;
	private final Supplier<C> defaultComponent;
	private final Function<C, T> getter;
	private final BiFunction<C, T, C> setter;
	
	public NBTComponentTagReference(String tag, Codec<C> codec, Supplier<T> defaultValue, Supplier<C> defaultComponent, Function<C, T> getter, BiFunction<C, T, C> setter) {
		this.tag = tag;
		this.codec = codec;
		this.defaultValue = defaultValue;
		this.defaultComponent = defaultComponent;
		this.getter = getter;
		this.setter = setter;
	}
	public NBTComponentTagReference(String tag, Codec<C> codec, Supplier<T> defaultValue, Function<C, T> getter, Function<T, C> setter) {
		this(tag, codec, defaultValue, null, getter, (componentValue, value) -> setter.apply(value));
	}
	
	@Override
	public T get(NbtCompound object) {
		return codec.decode(NbtOps.INSTANCE, object.get(tag)).result().map(Pair::getFirst).map(getter).orElseGet(defaultValue);
	}
	
	@Override
	public void set(NbtCompound object, T value) {
		C componentValue = (defaultComponent == null ? null :
			codec.decode(NbtOps.INSTANCE, object.get(tag)).result().map(Pair::getFirst).orElseGet(defaultComponent));
		object.put(tag, codec.encodeStart(NbtOps.INSTANCE, setter.apply(componentValue, value)).getOrThrow());
	}
	
}
