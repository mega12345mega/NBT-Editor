package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import com.luneruniverse.minecraft.mod.nbteditor.mixin.TextFieldWidgetMixin;
import com.luneruniverse.minecraft.mod.nbteditor.screens.Tickable;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;

public class MVTextFieldWidget extends TextFieldWidget implements Tickable, MVElement {
	
	/**
	 * The selection highlight doesn't move when {@link MatrixStack#translate(double, double, double)} is called <br />
	 * Via {@link TextFieldWidgetMixin}, the vertex calls are redirected to take this matrix into account
	 * As of 1.19.4, this is fixed
	 */
	public static MVMatrix4f matrix;
	
	protected MVTooltip tooltip;
	
	public MVTextFieldWidget(int x, int y, int width, int height, TextFieldWidget copyFrom) {
		super(MainUtil.client.textRenderer, x, y, width, height, copyFrom, TextInst.of(""));
	}
	public MVTextFieldWidget(int x, int y, int width, int height) {
		super(MainUtil.client.textRenderer, x, y, width, height, TextInst.of(""));
	}
	
	public MVTextFieldWidget tooltip(MVTooltip tooltip) {
		this.tooltip = tooltip;
		Version.newSwitch()
				.range("1.19.3", null, () -> setTooltip(tooltip == null ? null : tooltip.toNewTooltip()))
				.range(null, "1.19.2", () -> {})
				.run();
		return this;
	}
	
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		try {
			matrix = MVMatrix4f.getPositionMatrix(matrices.peek()).copy();
			MVDrawableHelper.super_render(MVTextFieldWidget.class, this, matrices, mouseX, mouseY, delta);
			Version.newSwitch()
					.range("1.19.3", null, () -> {})
					.range(null, "1.19.2", () -> {
						if (hovered)
							method_25352(matrices, mouseX, mouseY);
					})
					.run();
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
	
	public void method_25352(MatrixStack matrices, int mouseX, int mouseY) { // renderTooltip
		if (tooltip != null)
			tooltip.render(matrices, mouseX, mouseY);
	}
	
	@Override
	@Deprecated
	public void setFocused(boolean focused) {
		setMultiFocused(focused);
	}
	@Override
	@Deprecated
	public boolean isFocused() {
		return isMultiFocused();
	}
	
}
