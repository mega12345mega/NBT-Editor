package com.luneruniverse.minecraft.mod.nbteditor.screens.configurable;

import java.util.ArrayList;
import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.ExtendableButtonWidget;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVTooltip;

import net.minecraft.text.Text;

public class ConfigValueBoolean extends ExtendableButtonWidget implements ConfigValue<Boolean, ConfigValueBoolean> {
	
	private final Text on;
	private final Text off;
	private final boolean defaultValue;
	private boolean value;
	private MVTooltip tooltip;
	
	private final List<ConfigValueListener<ConfigValueBoolean>> onChanged;
	
	public ConfigValueBoolean(boolean value, boolean defaultValue, int width, Text on, Text off, MVTooltip tooltip) {
		super(0, 0, width, 20, value ? on : off, btn -> ((ConfigValueBoolean) btn).setValue(!((ConfigValueBoolean) btn).getValue()), tooltip);
		this.on = on;
		this.off = off;
		this.value = value;
		this.defaultValue = defaultValue;
		this.tooltip = tooltip;
		this.onChanged = new ArrayList<>();
	}
	public ConfigValueBoolean(boolean value, boolean defaultValue, int width, Text on, Text off) {
		this(value, defaultValue, width, on, off, MVTooltip.EMPTY);
	}
	private ConfigValueBoolean(boolean value, boolean defaultValue, int width, Text on, Text off, MVTooltip tooltipSupplier, List<ConfigValueListener<ConfigValueBoolean>> onChanged) {
		this(value, defaultValue, width, on, off, tooltipSupplier);
		this.onChanged.addAll(onChanged);
	}
	
	@Override
	public Boolean getDefaultValue() {
		return defaultValue;
	}
	
	@Override
	public void setValue(Boolean value) {
		this.value = value;
		setMessage(value ? on : off);
		onChanged.forEach(listener -> listener.onValueChanged(this));
	}
	@Override
	public Boolean getValue() {
		return value;
	}
	@Override
	public boolean isValueValid() {
		return true;
	}
	@Override
	public ConfigValueBoolean addValueListener(ConfigValueListener<ConfigValueBoolean> listener) {
		onChanged.add(listener);
		return this;
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
	public ConfigValueBoolean clone(boolean defaults) {
		return new ConfigValueBoolean(value, defaultValue, width, on, off, tooltip, onChanged);
	}
	
}
