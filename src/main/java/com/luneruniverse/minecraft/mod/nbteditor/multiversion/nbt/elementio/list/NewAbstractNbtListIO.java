package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.elementio.list;

import java.util.Optional;
import java.util.stream.Stream;

import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.NbtElement;

public class NewAbstractNbtListIO implements AbstractNbtListIO {
	
	@Override
	public Optional<Byte> getHeldType(AbstractNbtList list) {
		byte heldType = (byte) 0;
		for (NbtElement element : list) {
			if (heldType == 0)
				heldType = element.getType();
			else if (heldType != element.getType())
				return Optional.empty();
		}
		return Optional.of(heldType);
	}
	
	@Override
	public int size(AbstractNbtList list) {
		return list.size();
	}
	
	@Override
	public boolean isEmpty(AbstractNbtList list) {
		return list.isEmpty();
	}
	
	@Override
	public Iterable<NbtElement> iterable(AbstractNbtList list) {
		return list;
	}
	
	@Override
	public Stream<NbtElement> stream(AbstractNbtList list) {
		return list.stream();
	}
	
	@Override
	public NbtElement get(AbstractNbtList list, int index) {
		return list.method_10534(index);
	}
	
	@Override
	public void add(AbstractNbtList list, int index, NbtElement element) {
		list.addElement(index, element);
	}
	
	@Override
	public void add(AbstractNbtList list, NbtElement element) {
		list.addElement(list.size(), element);
	}
	
	@Override
	public void set(AbstractNbtList list, int index, NbtElement element) {
		list.setElement(index, element);
	}
	
	@Override
	public NbtElement remove(AbstractNbtList list, int index) {
		return list.method_10536(index);
	}
	
	@Override
	public void clear(AbstractNbtList list) {
		list.clear();
	}
	
}
