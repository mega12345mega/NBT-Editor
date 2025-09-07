package com.luneruniverse.minecraft.mod.nbteditor.screens.factories;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalItem;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.ScreenTexts;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.screens.LocalEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigBar;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigButton;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigCategory;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigHiddenData;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigItem;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigPanel;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigValueBoolean;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.ItemTagReferences;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.hideflags.HideFlag;

public class HideFlagsScreen extends LocalEditorScreen<LocalItem> {
	
	private final ConfigCategory config;
	private ConfigPanel panel;
	
	public HideFlagsScreen(ItemReference ref) {
		super(TextInst.of("Hide Flags"), ref);
		
		Map<HideFlag, Boolean> flags = ItemTagReferences.HIDE_FLAGS.get(localNBT.getEditableItem());
		
		config = new ConfigCategory(TextInst.translatable("nbteditor.hide_flags"));
		
		if (HideFlag.TOOLTIP != null) {
			config.setConfigurable("" + Math.random(), new ConfigHiddenData<>(
					new ConfigItem<>(HideFlag.TOOLTIP.getName(),
							new ConfigValueBoolean(flags.get(HideFlag.TOOLTIP), false, 100, ScreenTexts.ON, ScreenTexts.OFF)),
					HideFlag.TOOLTIP, (flag, defaults) -> flag));
		}
		
		List<ConfigValueBoolean> mainFlagConfigs = new ArrayList<>();
		Consumer<Boolean> setAll = hidden -> mainFlagConfigs.forEach(flagConfig -> flagConfig.setValue(hidden));
		
		ConfigBar globalConfig = new ConfigBar();
		globalConfig.setConfigurable("show_all", new ConfigButton(100, TextInst.translatable("nbteditor.hide_flags.show_all"), btn -> setAll.accept(false)));
		globalConfig.setConfigurable("hide_all", new ConfigButton(100, TextInst.translatable("nbteditor.hide_flags.hide_all"), btn -> setAll.accept(true)));
		config.setConfigurable("global", globalConfig);
		
		for (Map.Entry<HideFlag, Boolean> flag : flags.entrySet()) {
			if (flag.getKey() == HideFlag.TOOLTIP)
				continue;
			ConfigValueBoolean flagConfig = new ConfigValueBoolean(flag.getValue(), false, 100, ScreenTexts.ON, ScreenTexts.OFF);
			mainFlagConfigs.add(flagConfig);
			config.setConfigurable("" + Math.random(), new ConfigHiddenData<>(
					new ConfigItem<>(flag.getKey().getName(), flagConfig),
					flag.getKey(), (flag2, defaults) -> flag2));
		}
		
		config.addValueListener(source -> {
			config.getConfigurables().forEach((flagName, path) -> {
				if (path instanceof ConfigHiddenData configHiddenData &&
						((ConfigItem<?>) configHiddenData.getVisible()).getValue() == source) {
					ItemTagReferences.HIDE_FLAGS.set(localNBT.getEditableItem(),
							Map.of((HideFlag) configHiddenData.getData(), (Boolean) source.getValidValue()));
					checkSave();
				}
			});
		});
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
