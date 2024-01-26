package com.luneruniverse.minecraft.mod.nbteditor.screens.widgets;

import java.util.function.Predicate;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.widget.TextFieldWidget;

public class StringInput extends GroupWidget implements InputOverlay.Input<String> {
	
	private final String initialValue;
	private final Predicate<String> valueValidator;
	private TextFieldWidget value;
	private boolean valid;
	
	public StringInput(String initialValue, Predicate<String> valueValidator) {
		this.initialValue = initialValue;
		this.valueValidator = valueValidator;
	}
	
	@Override
	public void init(int x, int y) {
		String prevValue = (value == null ? initialValue : value.getText());
		value = new TextFieldWidget(MainUtil.client.textRenderer, x, y, 204, 16, TextInst.of(""));
		value.setMaxLength(Integer.MAX_VALUE);
		value.setText(prevValue);
		addWidget(value);
		setFocused(value);
		
		value.setChangedListener(str -> valid = valueValidator.test(str));
		valid = valueValidator.test(value.getText());
	}
	
	@Override
	public String getValue() {
		return value.getText();
	}
	
	@Override
	public boolean isValid() {
		return valid;
	}
	
	@Override
	public int getWidth() {
		return value.getWidth();
	}
	
	@Override
	public int getHeight() {
		return value.getHeight();
	}
	
}
