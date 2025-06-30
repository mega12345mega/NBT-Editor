package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.util.Identifier;

class MVTexturedButtonWidget_1_20_2 extends ButtonWidget {
	
	protected final Identifier texture;
	protected final int u;
	protected final int v;
	protected final int hoveredVOffset;
	protected final int textureWidth;
	protected final int textureHeight;
	
	public MVTexturedButtonWidget_1_20_2(int x, int y, int width, int height, int u, int v, int hoveredVOffset,
			Identifier texture, int textureWidth, int textureHeight, ButtonWidget.PressAction pressAction) {
		super(x, y, width, height, ScreenTexts.EMPTY, pressAction, DEFAULT_NARRATION_SUPPLIER);
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
		this.u = u;
		this.v = v;
		this.hoveredVOffset = hoveredVOffset;
		this.texture = texture;
	}
	
	@Override
	public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		drawTexture(context, this.texture, getX(), getY(), this.u, this.v, this.hoveredVOffset, this.width, this.height,
				this.textureWidth, this.textureHeight);
	}
	
	public void drawTexture(DrawContext context, Identifier texture, int x, int y, int u, int v, int hoveredVOffset,
			int width, int height, int textureWidth, int textureHeight) {
		int i = v;
		if (!isNarratable()) {
			i += hoveredVOffset * 2;
		} else if (isSelected()) {
			i += hoveredVOffset;
		}
		RenderSystem.enableDepthTest();
		MVDrawableHelper.drawTexture(MVDrawableHelper.getMatrices(context), texture, x, y, u, i, width, height, textureWidth, textureHeight);
	}
	
}