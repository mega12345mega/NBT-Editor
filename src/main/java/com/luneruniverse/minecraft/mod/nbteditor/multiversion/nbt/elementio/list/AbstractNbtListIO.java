package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.elementio.list;

import java.util.Optional;
import java.util.stream.Stream;

import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.NbtElement;

public interface AbstractNbtListIO {
	public Optional<Byte> getHeldType(AbstractNbtList list);
	public int size(AbstractNbtList list);
	public boolean isEmpty(AbstractNbtList list);
	public Iterable<NbtElement> iterable(AbstractNbtList list);
	public Stream<NbtElement> stream(AbstractNbtList list);
	public NbtElement get(AbstractNbtList list, int index);
	public void add(AbstractNbtList list, int index, NbtElement element);
	public void add(AbstractNbtList list, NbtElement element);
	public void set(AbstractNbtList list, int index, NbtElement element);
	public NbtElement remove(AbstractNbtList list, int index);
	public void clear(AbstractNbtList list);
}
