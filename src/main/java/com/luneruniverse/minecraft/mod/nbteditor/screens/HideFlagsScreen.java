package com.luneruniverse.minecraft.mod.nbteditor.screens;

import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigButton;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigCategory;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigItem;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigPanel;
import com.luneruniverse.minecraft.mod.nbteditor.screens.configurable.ConfigValueBoolean;
import com.luneruniverse.minecraft.mod.nbteditor.util.ItemReference;

import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class HideFlagsScreen extends ItemEditorScreen {
	
	private enum Flag {
		ENCHANTMENTS(Text.translatable("nbteditor.flag.enchantments"), 1),
		ATTRIBUTE_MODIFIERS(Text.translatable("nbteditor.flag.attribute_modifiers"), 2),
		UNBREAKABLE(Text.translatable("nbteditor.flag.unbreakable"), 4),
		CAN_DESTORY(Text.translatable("nbteditor.flag.can_destroy"), 8),
		CAN_PLACE_ON(Text.translatable("nbteditor.flag.can_place_on"), 16),
		MISC(Text.translatable("nbteditor.flag.misc"), 32),
		DYED_COLOR(Text.translatable("nbteditor.flag.dyed_color"), 64);
		
		private final Text text;
		private final int code;
		
		private Flag(Text text, int code) {
			this.text = text;
			this.code = code;
		}
		
		public Text getText() {
			return text;
		}
		
		public int toggle(int code) {
			return (code & ~this.code) | (~code & this.code);
		}
		public boolean isEnabled(int code) {
			return (code & this.code) != 0;
		}
	}
	
	
	
	private final ConfigCategory config;
	private ConfigPanel panel;
	
	@SuppressWarnings("unchecked")
	public HideFlagsScreen(ItemReference ref) {
		super(Text.of("Hide Flags"), ref);
		
		config = new ConfigCategory(Text.translatable("nbteditor.hideflags"));
		config.setConfigurable("disable_all", new ConfigButton(100, Text.translatable("nbteditor.hideflags.disable_all"), btn -> setCode(0)));
		config.setConfigurable("enable_all", new ConfigButton(100, Text.translatable("nbteditor.hideflags.enable_all"), btn -> setCode(127)));
		
		int code = item.getOrCreateNbt().getInt("HideFlags");
		for (Flag flag : Flag.values())
			config.setConfigurable(flag.name(), new ConfigItem<>(flag.getText(), new ConfigValueBoolean(flag.isEnabled(code), false, 100, ScreenTexts.ON, ScreenTexts.OFF)));
		
		config.addValueListener(source -> {
			int newCode = 0;
			for (Flag flag : Flag.values()) {
				if (((ConfigItem<ConfigValueBoolean>) config.getConfigurable(flag.name())).getValue().getValidValue())
					newCode = flag.toggle(newCode);
			}
			item.getOrCreateNbt().putInt("HideFlags", newCode);
			checkSave();
		});
	}
	@SuppressWarnings("unchecked")
	private void setCode(int code) {
		for (Flag flag : Flag.values())
			((ConfigItem<ConfigValueBoolean>) config.getConfigurable(flag.name())).getValue().setValue(flag.isEnabled(code));
	}
	
	@Override
	protected void initEditor() {
		ConfigPanel newPanel = addDrawableChild(new ConfigPanel(16, 64, width - 32, height - 80, config));
		if (panel != null)
			newPanel.setScroll(panel.getScroll());
		panel = newPanel;
	}
	
}
