package com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVComponentType;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Unit;

public class ComponentTagReference<T, C> implements TagReference<T, ItemStack> {
	
	public static <C> ComponentTagReference<Boolean, C> forExistance(MVComponentType<C> component, Supplier<C> supplier) {
		return new ComponentTagReference<>(component, null, componentValue -> componentValue != null, (componentValue, value) -> {
			if (value == null)
				value = false;
			if ((componentValue != null) == value)
				return componentValue;
			return value ? supplier.get() : null;
		});
	}
	public static ComponentTagReference<Boolean, Unit> forExistance(MVComponentType<Unit> component) {
		return forExistance(component, () -> Unit.INSTANCE);
	}
	
	private final MVComponentType<C> component;
	private final Supplier<C> defaultComponent;
	private final Function<C, T> getter;
	private final BiFunction<C, T, C> setter;
	private boolean passNullValue;
	
	public ComponentTagReference(MVComponentType<C> component, Supplier<C> defaultComponent, Function<C, T> getter, BiFunction<C, T, C> setter) {
		this.component = component;
		this.defaultComponent = defaultComponent;
		this.getter = getter;
		this.setter = setter;
		this.passNullValue = false;
	}
	public ComponentTagReference(MVComponentType<C> component, Supplier<C> defaultComponent, Function<C, T> getter, Function<T, C> setter) {
		this(component, defaultComponent, getter, (componentValue, value) -> setter.apply(value));
	}
	
	public ComponentTagReference<T, C> passNullValue() {
		passNullValue = true;
		return this;
	}
	
	@Override
	public T get(ItemStack object) {
		C componentValue = object.get(component);
		if (componentValue == null && defaultComponent != null)
			componentValue = defaultComponent.get();
		return getter.apply(componentValue);
	}
	
	@Override
	public void set(ItemStack object, T value) {
		if (value == null && !passNullValue) {
			object.set(component, null);
			return;
		}
		C componentValue = object.get(component);
		if (componentValue == null && defaultComponent != null)
			componentValue = defaultComponent.get();
		object.set(component, setter.apply(componentValue, value));
	}
	
}
