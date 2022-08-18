package com.luneruniverse.minecraft.mod.nbteditor.screens.configurable;

public interface ConfigValue<T, V extends ConfigValue<T, V>> extends Configurable<V> {
	public T getDefaultValue();
	public void setValue(T value);
	public T getValue();
	public default T getValidValue() {
		return isValueValid() ? getValue() : getDefaultValue();
	}
	public V addValueListener(ConfigValueListener<V> listener);
}
