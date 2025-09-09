package com.luneruniverse.minecraft.mod.nbteditor.multiversion.mixin;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Mixin;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.MVAbstractNbtListParent;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.MVNbtCompoundParent;

import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLongArray;

@Mixin(AbstractNbtList.class)
public interface AbstractNbtListMixin extends MVAbstractNbtListParent {
	
	public default Optional<Byte> nbte$getHeldType() {
		if (MVNbtCompoundParent.NBT_CODE_REFACTORED) {
			byte heldType = (byte) 0;
			for (NbtElement element : (AbstractNbtList) (Object) this) {
				if (heldType == 0)
					heldType = element.getType();
				else if (heldType != element.getType())
					return Optional.empty();
			}
			return Optional.of(heldType);
		}
		return Optional.of(AbstractNbtList_getHeldType.get().invoke(this));
	}
	
	public default int nbte$size() {
		if (MVNbtCompoundParent.NBT_CODE_REFACTORED)
			return ((AbstractNbtList) (Object) this).size();
		return ((List<?>) this).size();
	}
	
	public default boolean nbte$isEmpty() {
		return nbte$size() == 0;
	}
	
	@SuppressWarnings("unchecked")
	public default Iterable<NbtElement> nbte$iterable() {
		return (Iterable<NbtElement>) this;
	}
	
	@SuppressWarnings("unchecked")
	public default Stream<NbtElement> nbte$stream() {
		if (MVNbtCompoundParent.NBT_CODE_REFACTORED)
			return ((AbstractNbtList) (Object) this).stream();
		return ((List<NbtElement>) this).stream();
	}
	
	public default NbtElement nbte$get(int index) {
		if (MVNbtCompoundParent.NBT_CODE_REFACTORED)
			return ((AbstractNbtList) (Object) this).method_10534(index);
		return (NbtElement) ((List<?>) this).get(index);
	}
	
	@SuppressWarnings("unchecked")
	public default void nbte$add(int index, NbtElement element) {
		if (MVNbtCompoundParent.NBT_CODE_REFACTORED)
			((AbstractNbtList) (Object) this).addElement(index, element);
		else
			((List<NbtElement>) this).add(cast(element));
	}
	public default void nbte$add(NbtElement element) {
		nbte$add(nbte$size(), element);
	}
	
	@SuppressWarnings("unchecked")
	public default void nbte$set(int index, NbtElement element) {
		if (MVNbtCompoundParent.NBT_CODE_REFACTORED)
			((AbstractNbtList) (Object) this).setElement(index, element);
		else
			((List<NbtElement>) this).set(index, cast(element));
	}
	
	public default NbtElement nbte$remove(int index) {
		if (MVNbtCompoundParent.NBT_CODE_REFACTORED)
			return ((AbstractNbtList) (Object) this).method_10536(index);
		return (NbtElement) ((List<?>) this).remove(index);
	}
	
	public default void nbte$clear() {
		if (MVNbtCompoundParent.NBT_CODE_REFACTORED)
			((AbstractNbtList) (Object) this).clear();
		else
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
