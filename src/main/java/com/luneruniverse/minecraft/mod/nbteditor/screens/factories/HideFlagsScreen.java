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

import net.minecraft.text.Text;

public class HideFlagsScreen extends LocalEditorScreen<LocalItem, ItemReference> {
	
	private enum Flag {
		ENCHANTMENTS(TextInst.translatable("nbteditor.hide_flags.enchantments"), 1),
		ATTRIBUTE_MODIFIERS(TextInst.translatable("nbteditor.hide_flags.attribute_modifiers"), 2),
		UNBREAKABLE(TextInst.translatable("nbteditor.hide_flags.unbreakable"), 4),
		CAN_DESTORY(TextInst.translatable("nbteditor.hide_flags.can_destroy"), 8),
		CAN_PLACE_ON(TextInst.translatable("nbteditor.hide_flags.can_place_on"), 16),
		MISC(TextInst.translatable("nbteditor.hide_flags.misc"), 32),
		DYED_COLOR(TextInst.translatable("nbteditor.hide_flags.dyed_color"), 64);
		
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
		super(TextInst.of("Hide Flags"), ref);
		
		config = new ConfigCategory(TextInst.translatable("nbteditor.hide_flags"));
		config.setConfigurable("disable_all", new ConfigButton(100, TextInst.translatable("nbteditor.hide_flags.show_all"), btn -> setCode(0)));
		config.setConfigurable("enable_all", new ConfigButton(100, TextInst.translatable("nbteditor.hide_flags.hide_all"), btn -> setCode(127)));
		
		int code = localNBT.getOrCreateNBT().getInt("HideFlags");
		for (Flag flag : Flag.values())
			config.setConfigurable(flag.name(), new ConfigItem<>(flag.getText(), new ConfigValueBoolean(flag.isEnabled(code), false, 100, ScreenTexts.ON, ScreenTexts.OFF)));
		
		config.addValueListener(source -> {
			int newCode = 0;
			for (Flag flag : Flag.values()) {
				if (((ConfigItem<ConfigValueBoolean>) config.getConfigurable(flag.name())).getValue().getValidValue())
					newCode = flag.toggle(newCode);
			}
			localNBT.getOrCreateNBT().putInt("HideFlags", newCode);
			checkSave();
		});
	}
	@SuppressWarnings("unchecked")
	private void setCode(int code) {
		for (Flag flag : Flag.values())
			((ConfigItem<ConfigValueBoolean>) config.getConfigurable(flag.name())).getValue().setValue(flag.isEnabled(code));
	}
	
	@Override
	protected FactoryLink<ItemReference> getFactoryLink() {
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
