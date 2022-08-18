package com.luneruniverse.minecraft.mod.nbteditor.screens.configurable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.luneruniverse.minecraft.mod.nbteditor.screens.NamedTextFieldWidget;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.text.Text;

public abstract class ConfigValueNumber<T extends Number, V extends ConfigValueNumber<T, V>> extends NamedTextFieldWidget implements ConfigValue<T, V> {
	
	protected interface Constructor<T extends Number, V extends ConfigValueNumber<T, V>> {
		V newInstance(T value, T defaultValue, T min, T max);
	}
	
	private final T defaultValue;
	private final T min;
	private final T max;
	private final Constructor<T, V> cloneImpl;
	
	protected final List<ConfigValueListener<V>> onChanged;
	
	@SuppressWarnings("unchecked")
	protected ConfigValueNumber(T value, T defaultValue, T min, T max, Constructor<T, V> cloneImpl) {
		super(MainUtil.client.textRenderer, 0, 0, 200, 20, Text.of(value + ""));
		setMaxLength(Integer.MAX_VALUE);
		name(Text.of(defaultValue + ""));
		setText(value + "");
		setTextPredicate(str -> {
			if (str.isEmpty() || str.equals("-") || str.equals("+"))
				return true;
			try {
				parse(str);
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		});
		
		this.defaultValue = defaultValue;
		this.min = min;
		this.max = max;
		this.cloneImpl = cloneImpl;
		this.onChanged = new ArrayList<>();
		
		super.setChangedListener(str -> {
			boolean valid = isValueValid();
			setValid(valid);
			if (valid)
				onChanged.forEach(listener -> listener.onValueChanged((V) this));
		});
	}
	
	@Override
	public T getDefaultValue() {
		return defaultValue;
	}
	
	@Override
	public void setValue(T value) {
		setText(value + "");
	}
	@Override
	public T getValue() {
		try {
			return parse(getText());
		} catch (NumberFormatException e) {
			setValue(defaultValue);
			return defaultValue;
		}
	}
	@Override
	public boolean isValueValid() {
		if (getText().isEmpty() || getText().equals("-") || getText().equals("+"))
			return false;
		T value = getValue();
		return min.doubleValue() <= value.doubleValue() && value.doubleValue() <= max.doubleValue();
	}
	@SuppressWarnings("unchecked")
	@Override
	public V addValueListener(ConfigValueListener<V> listener) {
		onChanged.add(listener);
		return (V) this;
	}
	@Override
	public void setChangedListener(Consumer<String> changedListener) {
		throw new UnsupportedOperationException("Use setValueListener instead!");
	}
	
	protected abstract T parse(String value) throws NumberFormatException;
	
	
	@Override
	public int getSpacingHeight() {
		return 20;
	}
	
	@Override
	public V clone(boolean defaults) {
		V output = cloneImpl.newInstance(defaults ? defaultValue : getValue(), defaultValue, min, max);
		output.onChanged.addAll(onChanged);
		return output;
	}
	
}
