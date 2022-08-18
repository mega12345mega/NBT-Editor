package com.luneruniverse.minecraft.mod.nbteditor.screens.configurable;

import org.jetbrains.annotations.TestOnly;

import com.luneruniverse.minecraft.mod.nbteditor.screens.SimpleTooltip;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

@TestOnly
public class ConfigTesterScreen extends Screen {
	
	public enum Test {
		V1,
		V2,
		V3,
		V4
	}
	
	private final ConfigCategory attributes;
	private ConfigPanel panel;
	
	public ConfigTesterScreen() {
		super(Text.of("Item Attributes"));
		
		attributes = new ConfigCategory();
		attributes.setConfigurable("subconfig", new ConfigCategory(Text.of("Sub Config")).setConfigurable("hi1", new ConfigItem<>(Text.of("HI1"), new ConfigValueText("value", "default"))).setConfigurable("hiii", new ConfigItem<>(Text.of("HIII"), new ConfigValueInteger(1, 1, 1, 1))));
		attributes.setConfigurable("hi2", new ConfigItem<>(Text.of("HI2"), new ConfigValueText("value2", "default2")));
		attributes.setConfigurable("hi3", new ConfigItem<>(Text.of("HI3"), new ConfigValueInteger(5, 4, 1, 10)));
		attributes.setConfigurable("hi1341341341234", new ConfigItem<>(Text.of("HI3"), new ConfigValueDouble(5, 4, 1, 10)));
		attributes.setConfigurable("hi5", new ConfigItem<>(Text.of("HI6"), new ConfigValueDropdownEnum<>(Test.V1, Test.V2, Test.class)));
		attributes.setConfigurable("hi4", new ConfigItem<>(Text.of("HI3"), new ConfigValueDouble(5, 4, 1, 10)));
		attributes.setConfigurable("h53141341341341fadf", new ConfigButton(150, Text.of("Hello World"), btn -> System.out.println("hi"), new SimpleTooltip(this, "atooltip.nottranslated")));
		attributes.setConfigurable("hi222222", new ConfigList(Text.of("some rndm list"), true, new ConfigItem<>(Text.of("new item"), new ConfigValueText("hi", "hiii"))).addConfigurable(new ConfigItem<>(Text.of("item1"), new ConfigValueText("", ""))).addConfigurable(new ConfigCategory(Text.of("item2")).setConfigurable("hi", new ConfigItem<>(Text.of("hiii"), new ConfigValueInteger(1, 1, 1, 1)))));
	}
	
	@Override
	protected void init() {
		ConfigPanel newPanel = addDrawableChild(new ConfigPanel(16, 16, width - 32, height - 32, attributes));
		if (panel != null)
			newPanel.setScroll(panel.getScroll());
		panel = newPanel;
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
	}
	
}
