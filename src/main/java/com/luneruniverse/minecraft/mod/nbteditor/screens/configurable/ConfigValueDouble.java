package com.luneruniverse.minecraft.mod.nbteditor.screens.configurable;

public class ConfigValueDouble extends ConfigValueNumber<Double, ConfigValueDouble> {
	
	public ConfigValueDouble(double value, double defaultValue, double min, double max) {
		super(value, defaultValue, min, max, ConfigValueDouble::new);
	}
	
	@Override
	protected Double parse(String value) throws NumberFormatException {
		return Double.parseDouble(value);
	}
	
}
