package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.elementio;

import java.util.Optional;
import java.util.stream.Stream;

import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.NbtElement;

/**
 * Allows abbreviating <code>ElementIOs.LIST.clear(list)</code> to <code>MVL.clear(list)</code>
 */
public class MVL {
	
	public static Optional<Byte> getHeldType(AbstractNbtList list) {
		return ElementIOs.LIST.getHeldType(list);
	}
	
	public static int size(AbstractNbtList list) {
		return ElementIOs.LIST.size(list);
	}
	
	public static boolean isEmpty(AbstractNbtList list) {
		return ElementIOs.LIST.isEmpty(list);
	}
	
	public static Iterable<NbtElement> iterable(AbstractNbtList list) {
		return ElementIOs.LIST.iterable(list);
	}
	
	public static Stream<NbtElement> stream(AbstractNbtList list) {
		return ElementIOs.LIST.stream(list);
	}
	
	public static NbtElement get(AbstractNbtList list, int index) {
		return ElementIOs.LIST.get(list, index);
	}
	
	public static void add(AbstractNbtList list, int index, NbtElement element) {
		ElementIOs.LIST.add(list, index, element);
	}
	
	public static void add(AbstractNbtList list, NbtElement element) {
		ElementIOs.LIST.add(list, element);
	}
	
	public static void set(AbstractNbtList list, int index, NbtElement element) {
		ElementIOs.LIST.set(list, index, element);
	}
	
	public static NbtElement remove(AbstractNbtList list, int index) {
		return ElementIOs.LIST.remove(list, index);
	}
	
	public static void clear(AbstractNbtList list) {
		ElementIOs.LIST.clear(list);
	}
	
}
