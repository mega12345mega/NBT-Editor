package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.elementio.list;

import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;

import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLongArray;

public class OldAbstractNbtListIO implements AbstractNbtListIO {
	
	private static final Reflection.MethodInvoker AbstractNbtList_getHeldType =
			Reflection.getMethod(AbstractNbtList.class, "method_10601", MethodType.methodType(byte.class));
	@Override
	public Optional<Byte> getHeldType(AbstractNbtList list) {
		return Optional.of(AbstractNbtList_getHeldType.invoke(list));
	}
	
	@Override
	public int size(AbstractNbtList list) {
		return ((List<?>) list).size();
	}
	
	@Override
	public boolean isEmpty(AbstractNbtList list) {
		return ((List<?>) list).isEmpty();
	}
	
	@Override
	public Iterable<NbtElement> iterable(AbstractNbtList list) {
		return list;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Stream<NbtElement> stream(AbstractNbtList list) {
		return ((List<NbtElement>) list).stream();
	}
	
	@Override
	public NbtElement get(AbstractNbtList list, int index) {
		return (NbtElement) ((List<?>) list).get(index);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void add(AbstractNbtList list, int index, NbtElement element) {
		((List<NbtElement>) list).add(index, cast(list, element));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void add(AbstractNbtList list, NbtElement element) {
		((List<NbtElement>) list).add(cast(list, element));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void set(AbstractNbtList list, int index, NbtElement element) {
		((List<NbtElement>) list).set(index, cast(list, element));
	}
	
	@Override
	public NbtElement remove(AbstractNbtList list, int index) {
		return (NbtElement) ((List<?>) list).remove(index);
	}
	
	@Override
	public void clear(AbstractNbtList list) {
		((List<?>) list).clear();
	}
	
	private NbtElement cast(AbstractNbtList list, NbtElement element) {
		if (list instanceof NbtByteArray || list instanceof NbtIntArray || list instanceof NbtLongArray) {
			if (element instanceof AbstractNbtNumber)
				return element;
			throw new ClassCastException("Cannot add a " + element.getClass().getName() + " to a " + list.getClass().getName());
		}
		
		if (list instanceof NbtList) {
			int heldType = getHeldType(list).get();
			if (heldType == 0 || heldType == element.getType())
				return element;
			throw new ClassCastException("Cannot add a " + element.getClass().getName());
		}
		
		throw new IllegalStateException("Unknown AbstractNbtList type: " + list.getClass().getName());
	}
	
}
