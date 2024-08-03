package com.luneruniverse.minecraft.mod.nbteditor.screens.configurable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.NamedTextFieldWidget;

public class ConfigValueNumber<T extends Number> extends NamedTextFieldWidget implements ConfigValue<T, ConfigValueNumber<T>> {
	
	public static ConfigValueNumber<Integer> forInt(int value, int defaultValue, int min, int max) {
		return new ConfigValueNumber<>(value, defaultValue, min, max, Integer::parseInt, null);
	}
	public static ConfigValueNumber<Double> forDouble(double value, double defaultValue, double min, double max) {
		return new ConfigValueNumber<>(value, defaultValue, min, max, Double::parseDouble, null);
	}
	
	
	private interface Parser<T extends Number> {
		T parse(String value) throws NumberFormatException;
	}
	
	private final T defaultValue;
	private final T min;
	private final T max;
	private final Parser<T> parser;
	
	private final List<ConfigValueListener<ConfigValueNumber<T>>> onChanged;
	
	private ConfigValueNumber(T value, T defaultValue, T min, T max, Parser<T> parser, List<ConfigValueListener<ConfigValueNumber<T>>> onChanged) {
		super(0, 0, 200, 20);
		setMaxLength(Integer.MAX_VALUE);
		name(TextInst.of(defaultValue + ""));
		setText(value + "");
		setTextPredicate(str -> {
			if (str.isEmpty() || str.equals("-") || str.equals("+"))
				return true;
			try {
				parser.parse(str);
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		});
		
		this.defaultValue = defaultValue;
		this.min = min;
		this.max = max;
		this.parser = parser;
		this.onChanged = new ArrayList<>();
		if (onChanged != null)
			this.onChanged.addAll(onChanged);
		
		super.setChangedListener(str -> {
			boolean valid = isValueValid();
			setValid(valid);
			if (valid)
				this.onChanged.forEach(listener -> listener.onValueChanged(this));
		});
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		boolean output = super.mouseClicked(mouseX, mouseY, button);
		setMultiFocused(output);
		return output;
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
			return parser.parse(getText());
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
	@Override
	public ConfigValueNumber<T> addValueListener(ConfigValueListener<ConfigValueNumber<T>> listener) {
		onChanged.add(listener);
		return this;
	}
	@Override
	public void setChangedListener(Consumer<String> changedListener) {
		throw new UnsupportedOperationException("Use addValueListener instead!");
	}
	
	
	@Override
	public int getSpacingWidth() {
		return this.width;
	}
	
	@Override
	public int getSpacingHeight() {
		return this.height;
	}
	
	@Override
	public ConfigValueNumber<T> clone(boolean defaults) {
		return new ConfigValueNumber<>(defaults ? defaultValue : getValue(), defaultValue, min, max, parser, onChanged);
	}
	
}
