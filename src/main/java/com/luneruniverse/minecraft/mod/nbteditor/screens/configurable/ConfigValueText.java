package com.luneruniverse.minecraft.mod.nbteditor.screens.configurable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.luneruniverse.minecraft.mod.nbteditor.screens.NamedTextFieldWidget;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.text.Text;

public class ConfigValueText extends NamedTextFieldWidget implements ConfigValue<String, ConfigValueText> {
	
	private final String defaultValue;
	private final List<ConfigValueListener<ConfigValueText>> onChanged;
	
	public ConfigValueText(String value, String defaultValue) {
		super(MainUtil.client.textRenderer, 0, 0, 200, 20, Text.of(value == null ? "" : value));
		setMaxLength(Integer.MAX_VALUE);
		name(Text.of(defaultValue));
		setText(value == null ? "" : value);
		
		this.defaultValue = defaultValue;
		this.onChanged = new ArrayList<>();
		
		super.setChangedListener(newValue -> {
			onChanged.forEach(listener -> listener.onValueChanged(this));
		});
	}
	private ConfigValueText(String value, String defaultValue, List<ConfigValueListener<ConfigValueText>> onChanged) {
		this(value, defaultValue);
		this.onChanged.addAll(onChanged);
	}
	
	@Override
	public String getDefaultValue() {
		return defaultValue;
	}
	
	@Override
	public void setValue(String value) {
		setText(value);
	}
	@Override
	public String getValue() {
		return getText();
	}
	@Override
	public boolean isValueValid() {
		return true;
	}
	@Override
	public ConfigValueText addValueListener(ConfigValueListener<ConfigValueText> listener) {
		onChanged.add(listener);
		return this;
	}
	@Override
	public void setChangedListener(Consumer<String> changedListener) {
		throw new UnsupportedOperationException("Use setValueListener instead!");
	}
	
	@Override
	public int getSpacingHeight() {
		return 20;
	}
	
	@Override
	public ConfigValueText clone(boolean defaults) {
		return new ConfigValueText(defaults ? defaultValue : getText(), defaultValue, onChanged);
	}
	
}