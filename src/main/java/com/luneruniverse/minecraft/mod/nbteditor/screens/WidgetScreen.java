package com.luneruniverse.minecraft.mod.nbteditor.screens;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class WidgetScreen extends OverlaySupportingScreen {
	
	public static <T extends Drawable & Element & Selectable> T setOverlayOrScreen(T overlay, double z, boolean restoreParent) {
		if (MainUtil.client.currentScreen instanceof OverlaySupportingScreen screen)
			screen.setOverlay(overlay, z);
		else
			MainUtil.client.setScreen(new WidgetScreen(TextInst.of(overlay.getClass().getName()), overlay, restoreParent));
		return overlay;
	}
	public static <T extends Drawable & Element & Selectable> T setOverlayOrScreen(T overlay, boolean restoreParent) {
		return setOverlayOrScreen(overlay, 0, restoreParent);
	}
	
	private Screen parent;
	
	private <T extends Drawable & Element & Selectable> WidgetScreen(Text title, T widget, boolean restoreParent) {
		super(title);
		setOverlay(widget);
		if (restoreParent)
			parent = MainUtil.client.currentScreen;
	}
	
	@Override
	public <T extends Drawable & Element> T setOverlay(T overlay, double z) {
		if (overlay == null)
			MainUtil.client.setScreen(parent);
		else
			parent = null;
		return super.setOverlay(overlay, z);
	}
	@Override
	public <T extends Screen> T setOverlayScreen(T overlay, double z) {
		if (overlay == null)
			MainUtil.client.setScreen(parent);
		else
			parent = null;
		return super.setOverlayScreen(overlay, z);
	}
	
	@Override
	protected void renderMain(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.renderBackground(matrices);
		super.renderMain(matrices, mouseX, mouseY, delta);
	}
	
}
