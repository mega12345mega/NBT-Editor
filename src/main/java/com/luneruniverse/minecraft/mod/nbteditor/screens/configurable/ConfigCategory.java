package com.luneruniverse.minecraft.mod.nbteditor.screens.configurable;

import net.minecraft.text.Text;

public class ConfigCategory extends ConfigGrouping<String, ConfigCategory> {
	
	public ConfigCategory(Text name) {
		super(name, ConfigCategory::new);
	}
	public ConfigCategory() {
		this(null);
	}
	
}
