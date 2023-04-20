package com.luneruniverse.minecraft.mod.nbteditor.screens;

import com.luneruniverse.minecraft.mod.nbteditor.mixin.TextFieldWidgetMixin;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionMisc;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class NamedTextFieldWidget extends TextFieldWidget {
	
	/**
	 * The selection highlight doesn't move when {@link MatrixStack#translate(double, double, double)} is called <br />
	 * Via {@link TextFieldWidgetMixin}, the vertex calls are redirected to take this matrix into account
	 * As of 1.19.4, this is fixed
	 */
	public static Object matrix;
	
	protected Text name;
	protected boolean valid;
	
	public NamedTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
		super(textRenderer, x, y, width, height, text);
		valid = true;
	}
	
	public NamedTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, TextFieldWidget copyFrom, Text text) {
		super(textRenderer, x, y, width, height, copyFrom, text);
		valid = true;
	}
	
	public NamedTextFieldWidget name(Text name) {
		this.name = name;
		return this;
	}
	
	public void setValid(boolean valid) {
		this.valid = valid;
	}
	public boolean isValid() {
		return valid;
	}
	
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		if (name != null)
			this.setSuggestion(this.getText().isEmpty() ? name.getString() : null);
		
		try {
			matrix = MultiVersionMisc.copyMatrix(MultiVersionMisc.getPositionMatrix(matrices.peek()));
			super.render(matrices, mouseX, mouseY, delta);
		} finally {
			matrix = null;
		}
	}
	
}
