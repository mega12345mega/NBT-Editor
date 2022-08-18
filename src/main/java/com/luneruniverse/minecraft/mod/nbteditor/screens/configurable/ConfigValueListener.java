package com.luneruniverse.minecraft.mod.nbteditor.screens.configurable;

@FunctionalInterface
public interface ConfigValueListener<V extends ConfigValue<?, ?>> {
	public void onValueChanged(V source);
}
