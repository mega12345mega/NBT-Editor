package com.luneruniverse.minecraft.mod.nbteditor.screens.widgets;

import com.luneruniverse.minecraft.mod.nbteditor.mixin.TextFieldWidgetMixin;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMatrix4f;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.screens.Tickable;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class NamedTextFieldWidget extends TextFieldWidget implements Tickable {
	
	/**
	 * The selection highlight doesn't move when {@link MatrixStack#translate(double, double, double)} is called <br />
	 * Via {@link TextFieldWidgetMixin}, the vertex calls are redirected to take this matrix into account
	 * As of 1.19.4, this is fixed
	 */
	public static MVMatrix4f matrix;
	
	protected Text name;
	protected boolean valid;
	
	public NamedTextFieldWidget(int x, int y, int width, int height, TextFieldWidget copyFrom) {
		super(MainUtil.client.textRenderer, x, y, width, height, copyFrom, TextInst.of(""));
		valid = true;
	}
	public NamedTextFieldWidget(int x, int y, int width, int height) {
		this(x, y, width, height, null);
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
			this.setSuggestion(text.isEmpty() ? name.getString() : null);
		
		try {
			matrix = MVMatrix4f.getPositionMatrix(matrices.peek()).copy();
			MVDrawableHelper.super_render(NamedTextFieldWidget.class, this, matrices, mouseX, mouseY, delta);
		} finally {
			matrix = null;
		}
	}
	public final void method_25394(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		render(matrices, mouseX, mouseY, delta);
	}
	@Override
	public final void render(DrawContext context, int mouseX, int mouseY, float delta) {
		render(MVDrawableHelper.getMatrices(context), mouseX, mouseY, delta);
	}
	
	protected boolean shouldShowName() {
		return true;
	}
	
}
