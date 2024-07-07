package com.luneruniverse.minecraft.mod.nbteditor.screens.factories;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalItem;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.ScreenTexts;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.screens.LocalEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigButton;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigCategory;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigItem;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigPanel;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigValueBoolean;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.HideFlag;

public class HideFlagsScreen extends LocalEditorScreen<LocalItem> {
	
	private final ConfigCategory config;
	private ConfigPanel panel;
	
	@SuppressWarnings("unchecked")
	public HideFlagsScreen(ItemReference ref) {
		super(TextInst.of("Hide Flags"), ref);
		
		config = new ConfigCategory(TextInst.translatable("nbteditor.hide_flags"));
		config.setConfigurable("disable_all", new ConfigButton(100, TextInst.translatable("nbteditor.hide_flags.show_all"), btn -> setCode(0)));
		config.setConfigurable("enable_all", new ConfigButton(100, TextInst.translatable("nbteditor.hide_flags.hide_all"), btn -> setCode(127)));
		
		int code = localNBT.getOrCreateNBT().getInt("HideFlags");
		for (HideFlag flag : HideFlag.values())
			config.setConfigurable(flag.name(), new ConfigItem<>(flag.getText(), new ConfigValueBoolean(flag.isEnabled(code), false, 100, ScreenTexts.ON, ScreenTexts.OFF)));
		
		config.addValueListener(source -> {
			int newCode = 0;
			for (HideFlag flag : HideFlag.values()) {
				if (((ConfigItem<ConfigValueBoolean>) config.getConfigurable(flag.name())).getValue().getValidValue())
					newCode = flag.toggle(newCode);
			}
			localNBT.getOrCreateNBT().putInt("HideFlags", newCode);
			checkSave();
		});
	}
	@SuppressWarnings("unchecked")
	private void setCode(int code) {
		for (HideFlag flag : HideFlag.values())
			((ConfigItem<ConfigValueBoolean>) config.getConfigurable(flag.name())).getValue().setValue(flag.isEnabled(code));
	}
	
	@Override
	protected FactoryLink<LocalItem> getFactoryLink() {
		return new FactoryLink<>("nbteditor.display", DisplayScreen::new);
	}
	
	@Override
	protected void initEditor() {
		ConfigPanel newPanel = addDrawableChild(new ConfigPanel(16, 64, width - 32, height - 80, config));
		if (panel != null)
			newPanel.setScroll(panel.getScroll());
		panel = newPanel;
	}
	
}
