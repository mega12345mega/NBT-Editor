package com.luneruniverse.minecraft.mod.nbteditor;

import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import net.minecraft.client.gui.screen.Screen;

public class NBTEditorModMenuApi implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return new ConfigScreenFactory<Screen>() {
			@Override
			public Screen create(Screen parent) {
				return new ConfigScreen(parent);
			}
		};
	}
}
