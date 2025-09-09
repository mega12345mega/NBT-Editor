package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt;

import java.lang.invoke.MethodType;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;

import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.NbtElement;

public interface MVAbstractNbtListParent {
	
	static final Supplier<Reflection.MethodInvoker> AbstractNbtList_getHeldType =
			Reflection.getOptionalMethod(AbstractNbtList.class, "method_10601", MethodType.methodType(byte.class));
	public default Optional<Byte> nbte$getHeldType() {
		throw new RuntimeException("Missing implementation for MVAbstractNbtListParent#nbte$getHeldType");
	}
	
	public default int nbte$size() {
		throw new RuntimeException("Missing implementation for MVAbstractNbtListParent#nbte$size");
	}
	
	public default boolean nbte$isEmpty() {
		throw new RuntimeException("Missing implementation for MVAbstractNbtListParent#nbte$isEmpty");
	}
	
	public default Iterable<NbtElement> nbte$iterable() {
		throw new RuntimeException("Missing implementation for MVAbstractNbtListParent#nbte$iterable");
	}
	
	public default Stream<NbtElement> nbte$stream() {
		throw new RuntimeException("Missing implementation for MVAbstractNbtListParent#nbte$stream");
	}
	
	public default NbtElement nbte$get(int index) {
		throw new RuntimeException("Missing implementation for MVAbstractNbtListParent#nbte$get");
	}
	
	public default void nbte$add(int index, NbtElement element) {
		throw new RuntimeException("Missing implementation for MVAbstractNbtListParent#nbte$add");
	}
	public default void nbte$add(NbtElement element) {
		throw new RuntimeException("Missing implementation for MVAbstractNbtListParent#nbte$add");
	}
	
	public default void nbte$set(int index, NbtElement element) {
		throw new RuntimeException("Missing implementation for MVAbstractNbtListParent#nbte$set");
	}
	
	public default NbtElement nbte$remove(int index) {
		throw new RuntimeException("Missing implementation for MVAbstractNbtListParent#nbte$remove");
	}
	
	public default void nbte$clear() {
		throw new RuntimeException("Missing implementation for MVAbstractNbtListParent#nbte$clear");
	}
	
}
