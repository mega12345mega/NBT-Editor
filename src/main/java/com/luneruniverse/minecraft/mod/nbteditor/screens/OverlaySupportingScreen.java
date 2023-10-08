package com.luneruniverse.minecraft.mod.nbteditor.screens;

import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.InitializableOverlay;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class OverlaySupportingScreen extends TickableSupportingScreen {
	
	public static <T extends Drawable & Element> T setOverlayStatic(T overlay, double z) {
		return ((OverlaySupportingScreen) MainUtil.client.currentScreen).setOverlay(overlay, z);
	}
	public static <T extends Drawable & Element> T setOverlayStatic(T overlay) {
		return setOverlayStatic(overlay, 0);
	}
	public static <T extends Screen> T setOverlayScreenStatic(T overlay, double z) {
		return ((OverlaySupportingScreen) MainUtil.client.currentScreen).setOverlayScreen(overlay, z);
	}
	public static <T extends Screen> T setOverlayScreenStatic(T overlay) {
		return setOverlayScreenStatic(overlay, 0);
	}
	
	private Element overlay; // extends Drawable & Element
	private Screen overlayScreen;
	private double overlayZ;
	
	protected OverlaySupportingScreen(Text title) {
		super(title);
	}
	
	public <T extends Drawable & Element> T setOverlay(T overlay, double z) {
		this.overlay = overlay;
		this.overlayScreen = null;
		this.overlayZ = z;
		if (overlay instanceof InitializableOverlay<?> initable)
			initable.initUnchecked(this);
		return overlay;
	}
	public <T extends Drawable & Element> T setOverlay(T overlay) {
		return setOverlay(overlay, 0);
	}
	public <T extends Screen> T setOverlayScreen(T overlay, double z) {
		this.overlay = overlay;
		this.overlayScreen = overlay;
		this.overlayZ = z;
		overlay.init(client, width, height);
		if (overlay instanceof InitializableOverlay<?> initable)
			initable.initUnchecked(this);
		return overlay;
	}
	public <T extends Screen> T setOverlayScreen(T overlay) {
		return setOverlayScreen(overlay, 0);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Drawable & Element> T getOverlay() {
		return (T) overlay;
	}
	public Screen getOverlayScreen() {
		return overlayScreen;
	}
	public double getOverlayZ() {
		return overlayZ;
	}
	
	@Override
	protected void init() {
		if (overlayScreen != null)
			overlayScreen.init(client, width, height);
		if (overlay instanceof InitializableOverlay<?> initable)
			initable.initUnchecked(this);
	}
	
	@Override
	public final void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		int bgMouseX = (overlay == null ? mouseX : -314);
		int bgMouseY = (overlay == null ? mouseY : -314);
		renderMain(matrices, bgMouseX, bgMouseY, delta);
		if (overlay != null) {
			boolean translated = (overlayZ != 0);
			if (translated) {
				matrices.push();
				matrices.translate(0.0, 0.0, overlayZ);
			}
			((Drawable) overlay).render(matrices, mouseX, mouseY, delta);
			if (translated)
				matrices.pop();
		}
	}
	protected void renderMain(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);
	}
	
	@Override
	public void tick() {
		if (overlay != null) {
			if (overlay instanceof Tickable tickable)
				tickable.tick();
		} else
			super.tick();
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (overlay != null)
			return overlay.mouseClicked(mouseX, mouseY, button);
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (overlay != null)
			return overlay.mouseReleased(mouseX, mouseY, button);
		return super.mouseReleased(mouseX, mouseY, button);
	}
	
	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		if (overlay != null)
			overlay.mouseMoved(mouseX, mouseY);
		else
			super.mouseMoved(mouseX, mouseY);
	}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (overlay != null)
			return overlay.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (overlay != null)
			return overlay.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (overlay != null)
			return overlay.keyPressed(keyCode, scanCode, modifiers);
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	@Override
	public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		if (overlay != null)
			return overlay.keyReleased(keyCode, scanCode, modifiers);
		return super.keyReleased(keyCode, scanCode, modifiers);
	}
	
	@Override
	public boolean charTyped(char chr, int modifiers) {
		if (overlay != null)
			return overlay.charTyped(chr, modifiers);
		return super.charTyped(chr, modifiers);
	}
	
}
