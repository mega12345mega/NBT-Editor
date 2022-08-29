package com.luneruniverse.minecraft.mod.nbteditor.screens.configurable;

import net.minecraft.text.Text;

public class ConfigBar extends ConfigGroupingHorizontal<String, ConfigBar> {
	
	public ConfigBar(Text name) {
		super(name, ConfigBar::new);
	}
	public ConfigBar() {
		this(null);
	}
	
}
