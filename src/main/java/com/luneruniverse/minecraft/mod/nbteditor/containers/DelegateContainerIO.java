package com.luneruniverse.minecraft.mod.nbteditor.containers;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class DelegateContainerIO<I, O> implements ContainerIO<I> {
	
	public static <I, O> DelegateContainerIO<I, O> map(ContainerIO<O> delegate, Function<I, O> getter, BiConsumer<I, O> setter) {
		return new DelegateContainerIO<>((container, innerContainer) -> delegate, getter, setter);
	}
	
	public static <T> DelegateContainerIO<T, T> dynamic(Function<T, ContainerIO<T>> delegate) {
		return new DelegateContainerIO<>((container, innerContainer) -> delegate.apply(container),
				container -> container, (container, innerContainer) -> {});
	}
	
	private final BiFunction<I, O, ContainerIO<O>> delegate;
	private final Function<I, O> getter;
	private final BiConsumer<I, O> setter;
	
	public DelegateContainerIO(BiFunction<I, O, ContainerIO<O>> delegate, Function<I, O> getter, BiConsumer<I, O> setter) {
		this.delegate = delegate;
		this.getter = getter;
		this.setter = setter;
	}
	@SuppressWarnings("unchecked") // O is completely internal
	public DelegateContainerIO(ContainerIO<I> delegate) {
		this((container, innerContainer) -> (ContainerIO<O>) delegate, container -> (O) container, (container, innerContainer) -> {});
	}
	
	private <T> T get(I container, BiFunction<ContainerIO<O>, O, T> method) {
		O innerContainer = getter.apply(container);
		return method.apply(delegate.apply(container, innerContainer), innerContainer);
	}
	
	@Override
	public boolean isSupported(I container) {
		return get(container, (delegate, innerContainer) -> delegate != null && delegate.isSupported(innerContainer));
	}
	
	@Override
	public int getMaxSlots(I container) {
		return get(container, ContainerIO::getMaxSlots);
	}
	
	@Override
	public Identifier[] getTextures(I container) {
		return get(container, ContainerIO::getTextures);
	}
	
	@Override
	public ItemStack[] read(I container) {
		return get(container, ContainerIO::read);
	}
	
	@Override
	public int write(I container, ItemStack[] contents) {
		O innerContainer = getter.apply(container);
		int numWritten = delegate.apply(container, innerContainer).write(innerContainer, contents);
		setter.accept(container, innerContainer);
		return numWritten;
	}
	
	@Override
	public int getNumWritten(I container, ItemStack[] contents) {
		return get(container, (io, innerContainer) -> io.getNumWritten(innerContainer, contents));
	}
	
	@Override
	public int getWrittenSlotIndex(I container, ItemStack[] contents, int slot) {
		return get(container, (io, innerContainer) -> io.getWrittenSlotIndex(innerContainer, contents, slot));
	}
	
}
