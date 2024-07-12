package com.luneruniverse.minecraft.mod.nbteditor.screens.factories;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalItem;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.ScreenTexts;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.screens.LocalEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigBar;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigButton;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigCategory;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigItem;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigPanel;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigValueBoolean;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.HideFlag;

public class HideFlagsScreen extends LocalEditorScreen<LocalItem> {
	
	private final ConfigCategory config;
	private ConfigPanel panel;
	
	public HideFlagsScreen(ItemReference ref) {
		super(TextInst.of("Hide Flags"), ref);
		
		config = new ConfigCategory(TextInst.translatable("nbteditor.hide_flags"));
		
		if (HideFlag.TOOLTIP.isInThisVersion()) {
			config.setConfigurable(HideFlag.TOOLTIP.name(), new ConfigItem<>(HideFlag.TOOLTIP.getText(),
					new ConfigValueBoolean(HideFlag.TOOLTIP.get(localNBT.getItem()), false, 100, ScreenTexts.ON, ScreenTexts.OFF)));
		}
		
		ConfigBar globalConfig = new ConfigBar();
		globalConfig.setConfigurable("show_all", new ConfigButton(100, TextInst.translatable("nbteditor.hide_flags.show_all"), btn -> setAll(false)));
		globalConfig.setConfigurable("hide_all", new ConfigButton(100, TextInst.translatable("nbteditor.hide_flags.hide_all"), btn -> setAll(true)));
		config.setConfigurable("global", globalConfig);
		
		for (HideFlag flag : HideFlag.values()) {
			if (flag == HideFlag.TOOLTIP || !flag.isInThisVersion())
				continue;
			config.setConfigurable(flag.name(), new ConfigItem<>(flag.getText(),
					new ConfigValueBoolean(flag.get(localNBT.getItem()), false, 100, ScreenTexts.ON, ScreenTexts.OFF)));
		}
		
		config.addValueListener(source -> {
			config.getConfigurables().forEach((flagName, path) -> {
				if (path instanceof ConfigItem configItem && configItem.getValue() == source) {
					HideFlag flag = HideFlag.valueOf(flagName);
					flag.set(localNBT.getItem(), (Boolean) source.getValidValue());
					checkSave();
				}
			});
		});
	}
	@SuppressWarnings("unchecked")
	private void setAll(boolean hidden) {
		for (HideFlag flag : HideFlag.values()) {
			if (flag == HideFlag.TOOLTIP || !flag.isInThisVersion())
				continue;
			((ConfigItem<ConfigValueBoolean>) config.getConfigurable(flag.name())).getValue().setValue(hidden);
		}
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
