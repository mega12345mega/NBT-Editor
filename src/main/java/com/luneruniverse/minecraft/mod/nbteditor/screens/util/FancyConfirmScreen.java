package com.luneruniverse.minecraft.mod.nbteditor.screens.util;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.PassContainerSlotUpdates;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class FancyConfirmScreen extends ConfirmScreen implements PassContainerSlotUpdates {
	
	private Screen parent;
	
	public FancyConfirmScreen(BooleanConsumer callback, Text title, Text message, Text yesTranslated, Text noTranslated) {
		super(callback, title, message, yesTranslated, noTranslated);
		parent = MainUtil.client.currentScreen;
	}
	public FancyConfirmScreen(BooleanConsumer callback, Text title, Text message) {
		super(callback, title, message);
		parent = MainUtil.client.currentScreen;
	}
	
	public FancyConfirmScreen setParent(Screen parent) {
		this.parent = parent;
		return this;
	}
	
	@Override
	protected void init() {
		parent.init(client, width, height);
		super.init();
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		parent.render(matrices, -314, -314, delta);
		
		matrices.push();
		matrices.translate(0.0, 0.0, 500.0);
		MVDrawableHelper.super_render(FancyConfirmScreen.class, this, matrices, mouseX, mouseY, delta);
		MainUtil.renderLogo(matrices);
		matrices.pop();
	}
	public final void method_25394(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		render(matrices, mouseX, mouseY, delta);
	}
	@Override
	public final void render(DrawContext context, int mouseX, int mouseY, float delta) {
		render(MVDrawableHelper.getMatrices(context), mouseX, mouseY, delta);
	}
	
	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		MVDrawableHelper.renderBackground(parent, MVDrawableHelper.getMatrices(context));
	}
	
}
