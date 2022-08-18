package com.luneruniverse.minecraft.mod.nbteditor.screens.configurable;

public interface ConfigPath extends Configurable<ConfigPath> {
	public ConfigPath addValueListener(ConfigValueListener<ConfigValue<?, ?>> listener);
}
