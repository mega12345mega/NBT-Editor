package com.luneruniverse.minecraft.mod.nbteditor.screens;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionScreen;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.ScreenTexts;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigItem;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigList;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigPanel;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigPath;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigValueText;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;

public class ShortcutsScreen extends MultiVersionScreen {
	
	private static final ConfigItem<ConfigValueText> SHORTCUT_ENTRY = new ConfigItem<>(TextInst.of(""), new ConfigValueText("", ""));
	
	private final Screen parent;
	private final ConfigList config;
	private ConfigPanel panel;
	
	public ShortcutsScreen(Screen parent) {
		super(TextInst.translatable("nbteditor.config.shortcuts"));
		this.parent = parent;
		this.config = new ConfigList(TextInst.translatable("nbteditor.config.shortcuts").append(" - ")
				.append(TextInst.translatable("nbteditor.config.shortcuts.example")), false, SHORTCUT_ENTRY);
		for (String shortcut : ConfigScreen.getShortcuts()) {
			ConfigItem<ConfigValueText> entry = SHORTCUT_ENTRY.clone(true);
			entry.getValue().setValue(shortcut);
			this.config.addConfigurable(entry);
		}
	}
	
	@Override
	protected void init() {
		ConfigPanel newPanel = addDrawableChild(new ConfigPanel(16, 16, width - 32, height - 32, config));
		if (panel != null)
			newPanel.setScroll(panel.getScroll());
		panel = newPanel;
		
		this.addDrawableChild(MultiVersionMisc.newButton(this.width - 134, this.height - 36, 100, 20, ScreenTexts.DONE, btn -> close()));
	}
	
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
	}
	
	public void close() {
		client.setScreen(this.parent);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void removed() {
		Set<String> newShortcuts = new LinkedHashSet<>();
		for (ConfigPath entry : this.config.getConfigurables().values())
			newShortcuts.add(((ConfigItem<ConfigValueText>) entry).getValue().getValidValue());
		if (new HashSet<>(ConfigScreen.getShortcuts()).equals(newShortcuts))
			return;
		ConfigScreen.getShortcuts().clear();
		ConfigScreen.getShortcuts().addAll(newShortcuts);
		ClientCommandManager.reregisterClientCommands();
	}
	
}
