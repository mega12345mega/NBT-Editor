package com.luneruniverse.minecraft.mod.nbteditor.multiversion.mixin.toggled;

import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Mixin;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.MVAbstractNbtListParent;

import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLongArray;

@Mixin(AbstractNbtList.class)
public class AbstractNbtListMixin_1_21_4 implements MVAbstractNbtListParent {
	
	private static final Supplier<Reflection.MethodInvoker> AbstractNbtList_getHeldType =
			Reflection.getOptionalMethod(AbstractNbtList.class, "method_10601", MethodType.methodType(byte.class));
	public Optional<Byte> nbte$getHeldType() {
		return Optional.of(AbstractNbtList_getHeldType.get().invoke(this));
	}
	
	public int nbte$size() {
		return ((List<?>) this).size();
	}
	
	public boolean nbte$isEmpty() {
		return nbte$size() == 0;
	}
	
	@SuppressWarnings("unchecked")
	public Iterable<NbtElement> nbte$iterable() {
		return (Iterable<NbtElement>) this;
	}
	
	@SuppressWarnings("unchecked")
	public Stream<NbtElement> nbte$stream() {
		return ((List<NbtElement>) this).stream();
	}
	
	public NbtElement nbte$get(int index) {
		return (NbtElement) ((List<?>) this).get(index);
	}
	
	@SuppressWarnings("unchecked")
	public void nbte$add(int index, NbtElement element) {
		((List<NbtElement>) this).add(index, cast(element));
	}
	public void nbte$add(NbtElement element) {
		nbte$add(nbte$size(), element);
	}
	
	@SuppressWarnings("unchecked")
	public void nbte$set(int index, NbtElement element) {
		((List<NbtElement>) this).set(index, cast(element));
	}
	
	public NbtElement nbte$remove(int index) {
		return (NbtElement) ((List<?>) this).remove(index);
	}
	
	public void nbte$clear() {
		((List<?>) this).clear();
	}
	
	private NbtElement cast(NbtElement element) {
		if ((Object) this instanceof NbtByteArray || (Object) this instanceof NbtIntArray || (Object) this instanceof NbtLongArray) {
			if (element instanceof AbstractNbtNumber)
				return element;
			throw new ClassCastException("Cannot add a " + element.getClass().getName() + " to a " + this.getClass().getName());
		}
		
		if ((Object) this instanceof NbtList) {
			int heldType = nbte$getHeldType().get();
			if (heldType == 0 || heldType == element.getType())
				return element;
			throw new ClassCastException("Cannot add a " + element.getClass().getName());
		}
		
		throw new IllegalStateException("Unknown AbstractNbtList type: " + this.getClass().getName());
	}
	
}
