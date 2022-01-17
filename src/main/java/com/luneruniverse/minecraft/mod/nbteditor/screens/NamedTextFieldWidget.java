package com.luneruniverse.minecraft.mod.nbteditor.screens;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class NamedTextFieldWidget extends TextFieldWidget {
	
	protected Text name;
	
	public NamedTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
		super(textRenderer, x, y, width, height, text);
	}
	
	public NamedTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, TextFieldWidget copyFrom, Text text) {
		super(textRenderer, x, y, width, height, copyFrom, text);
	}
	
	public NamedTextFieldWidget name(Text name) {
		this.name = name;
		return this;
	}
	
	
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		if (name != null)
			this.setSuggestion(this.getText().isEmpty() ? name.getString() : null);
		super.render(matrices, mouseX, mouseY, delta);
	}
	
}
