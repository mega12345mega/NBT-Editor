package com.luneruniverse.minecraft.mod.nbteditor.multiversion.mixin.toggled;

import java.util.Optional;
import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Mixin;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.MVAbstractNbtListParent;

import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.NbtElement;

@Mixin(AbstractNbtList.class)
public interface AbstractNbtListMixin_1_21_5 extends MVAbstractNbtListParent {
	
	public default Optional<Byte> nbte$getHeldType() {
		byte heldType = (byte) 0;
		for (NbtElement element : (AbstractNbtList) (Object) this) {
			if (heldType == 0)
				heldType = element.getType();
			else if (heldType != element.getType())
				return Optional.empty();
		}
		return Optional.of(heldType);
	}
	
	public default int nbte$size() {
		return ((AbstractNbtList) (Object) this).size();
	}
	
	public default boolean nbte$isEmpty() {
		return nbte$size() == 0;
	}
	
	@SuppressWarnings("unchecked")
	public default Iterable<NbtElement> nbte$iterable() {
		return (Iterable<NbtElement>) this;
	}
	
	public default Stream<NbtElement> nbte$stream() {
		return ((AbstractNbtList) (Object) this).stream();
	}
	
	public default NbtElement nbte$get(int index) {
		return ((AbstractNbtList) (Object) this).method_10534(index);
	}
	
	public default void nbte$add(int index, NbtElement element) {
		((AbstractNbtList) (Object) this).addElement(index, element);
	}
	public default void nbte$add(NbtElement element) {
		nbte$add(nbte$size(), element);
	}
	
	public default void nbte$set(int index, NbtElement element) {
		((AbstractNbtList) (Object) this).setElement(index, element);
	}
	
	public default NbtElement nbte$remove(int index) {
		return ((AbstractNbtList) (Object) this).method_10536(index);
	}
	
	public default void nbte$clear() {
		((AbstractNbtList) (Object) this).clear();
	}
	
}
