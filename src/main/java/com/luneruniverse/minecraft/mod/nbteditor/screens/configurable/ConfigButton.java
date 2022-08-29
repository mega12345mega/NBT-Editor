package com.luneruniverse.minecraft.mod.nbteditor.screens.configurable;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ConfigButton extends ButtonWidget implements ConfigPath {
	
	@FunctionalInterface
	public interface PressAction {
		public void onPress(ConfigButton button);
	}
	
	private final PressAction onPress;
	
	public ConfigButton(int width, Text message, PressAction onPress, TooltipSupplier tooltipSupplier) {
		super(0, 0, width, 20, message, btn -> onPress.onPress((ConfigButton) btn), tooltipSupplier);
		this.onPress = onPress;
	}
	public ConfigButton(int width, Text message, PressAction onPress) {
		this(width, message, onPress, EMPTY);
	}
	
	@Override
	public boolean isValueValid() {
		return true;
	}
	@Override
	public ConfigButton addValueListener(ConfigValueListener<ConfigValue<?, ?>> listener) {
		return this; // "Value" never changes
	}
	
	
	@Override
	public int getSpacingWidth() {
		return this.width;
	}
	
	@Override
	public int getSpacingHeight() {
		return this.height;
	}
	
	@Override
	public ConfigButton clone(boolean defaults) {
		return new ConfigButton(this.width, this.getMessage(), onPress, this.tooltipSupplier);
	}
	
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return false; // Stop space from triggering the button
	}
	
}
