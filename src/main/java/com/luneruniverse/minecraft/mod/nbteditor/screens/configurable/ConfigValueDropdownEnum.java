package com.luneruniverse.minecraft.mod.nbteditor.screens.configurable;

import java.util.Arrays;
import java.util.List;

public class ConfigValueDropdownEnum<T extends Enum<T>> extends ConfigValueDropdown<T> {
	
	private final Class<T> enumClass;
	
	public ConfigValueDropdownEnum(T value, T defaultValue, Class<T> enumClass) {
		super(value, defaultValue, Arrays.asList(enumClass.getEnumConstants()));
		this.enumClass = enumClass;
	}
	private ConfigValueDropdownEnum(T value, T defaultValue, Class<T> enumClass, boolean open, List<ConfigValueListener<ConfigValueDropdown<T>>> onChanged) {
		this(value, defaultValue, enumClass);
		this.open = open;
		this.onChanged.addAll(onChanged);
	}
	
	public Class<T> getEnumClass() {
		return enumClass;
	}
	
	@Override
	public ConfigValueDropdownEnum<T> addValueListener(ConfigValueListener<ConfigValueDropdown<T>> listener) {
		super.addValueListener(listener);
		return this;
	}
	
	@Override
	public ConfigValueDropdownEnum<T> clone(boolean defaults) {
		return new ConfigValueDropdownEnum<>(defaults ? defaultValue : value, defaultValue, enumClass, open, onChanged);
	}
	
}
