package com.luneruniverse.minecraft.mod.nbteditor.screens.widgets;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVTextFieldWidget;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVTooltip;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class NamedTextFieldWidget extends MVTextFieldWidget {
	
	protected Text name;
	protected boolean valid;
	
	public NamedTextFieldWidget(int x, int y, int width, int height, TextFieldWidget copyFrom) {
		super(x, y, width, height, copyFrom);
		valid = true;
	}
	public NamedTextFieldWidget(int x, int y, int width, int height) {
		this(x, y, width, height, null);
	}
	
	@Override
	public NamedTextFieldWidget tooltip(MVTooltip tooltip) {
		super.tooltip(tooltip);
		return this;
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
		if (name != null && shouldShowName())
			setSuggestion(text.isEmpty() ? name.getString() : null);
		super.render(matrices, mouseX, mouseY, delta);
	}
	
	protected boolean shouldShowName() {
		return true;
	}
	
}
