package com.luneruniverse.minecraft.mod.nbteditor.screens.configurable;

import com.luneruniverse.minecraft.mod.nbteditor.screens.Tickable;

public interface ConfigPath extends Configurable<ConfigPath>, Tickable {
	public ConfigPath addValueListener(ConfigValueListener<ConfigValue<?, ?>> listener);
}
