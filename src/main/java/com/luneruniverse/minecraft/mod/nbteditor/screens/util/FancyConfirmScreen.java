package com.luneruniverse.minecraft.mod.nbteditor.screens.util;

import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class FancyConfirmScreen extends ConfirmScreen {
	
	private final Screen parent;
	
	public FancyConfirmScreen(BooleanConsumer callback, Text title, Text message, Text yesTranslated, Text noTranslated) {
		super(callback, title, message, yesTranslated, noTranslated);
		parent = MainUtil.client.currentScreen;
	}
	public FancyConfirmScreen(BooleanConsumer callback, Text title, Text message) {
		super(callback, title, message);
		parent = MainUtil.client.currentScreen;
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
		super.render(matrices, mouseX, mouseY, delta);
		MainUtil.renderLogo(matrices);
		matrices.pop();
	}
	
}
