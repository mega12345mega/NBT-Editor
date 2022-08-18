package com.luneruniverse.minecraft.mod.nbteditor.screens.configurable;

public class ConfigValueInteger extends ConfigValueNumber<Integer, ConfigValueInteger> {
	
	public ConfigValueInteger(int value, int defaultValue, int min, int max) {
		super(value, defaultValue, min, max, ConfigValueInteger::new);
	}
	
	@Override
	protected Integer parse(String value) throws NumberFormatException {
		return Integer.parseInt(value);
	}
	
}
