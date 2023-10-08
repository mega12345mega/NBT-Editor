package com.luneruniverse.minecraft.mod.nbteditor.screens.widgets;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.util.math.MatrixStack;

public class TranslatedGroupWidget extends GroupWidget {
	
	public static <T extends Drawable & Element> TranslatedGroupWidget forWidget(T widget, double x, double y, double z) {
		TranslatedGroupWidget output = new TranslatedGroupWidget(x, y, z) {
			@Override
			protected void renderPre(MatrixStack matrices, int mouseX, int mouseY, float delta) {
				setFocused(isMultiFocused() ? widget : null);
			}
		};
		output.addWidget(widget);
		return output;
	}
	
	private double x;
	private double y;
	private double z;
	
	public TranslatedGroupWidget(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	public TranslatedGroupWidget() {
		this(0, 0, 0);
	}
	
	public TranslatedGroupWidget setTranslation(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}
	
	public TranslatedGroupWidget addTranslation(double x, double y, double z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}
	
	@Override
	public final void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		matrices.push();
		matrices.translate(x, y, z);
		mouseX -= (int) x;
		mouseY -= (int) y;
		renderPre(matrices, mouseX, mouseY, delta);
		super.render(matrices, mouseX, mouseY, delta);
		renderPost(matrices, mouseX, mouseY, delta);
		matrices.pop();
	}
	protected void renderPre(MatrixStack matrices, int mouseX, int mouseY, float delta) {}
	protected void renderPost(MatrixStack matrices, int mouseX, int mouseY, float delta) {}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		mouseX -= x;
		mouseY -= y;
		return mouseClickedPre(mouseX, mouseY, button) ||
				super.mouseClicked(mouseX, mouseY, button) ||
				mouseClickedPost(mouseX, mouseY, button);
	}
	protected boolean mouseClickedPre(double mouseX, double mouseY, int button) {
		return false;
	}
	protected boolean mouseClickedPost(double mouseX, double mouseY, int button) {
		return false;
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		mouseX -= x;
		mouseY -= y;
		return mouseReleasedPre(mouseX, mouseY, button) ||
				super.mouseReleased(mouseX, mouseY, button) ||
				mouseReleasedPost(mouseX, mouseY, button);
	}
	protected boolean mouseReleasedPre(double mouseX, double mouseY, int button) {
		return false;
	}
	protected boolean mouseReleasedPost(double mouseX, double mouseY, int button) {
		return false;
	}
	
	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		mouseX -= x;
		mouseY -= y;
		mouseMovedPre(mouseX, mouseY);
		super.mouseMoved(mouseX, mouseY);
		mouseMovedPost(mouseX, mouseY);
	}
	protected void mouseMovedPre(double mouseX, double mouseY) {}
	protected void mouseMovedPost(double mouseX, double mouseY) {}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		mouseX -= x;
		mouseY -= y;
		return mouseDraggedPre(mouseX, mouseY, button, deltaX, deltaY) ||
				super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY) ||
				mouseDraggedPost(mouseX, mouseY, button, deltaX, deltaY);
	}
	protected boolean mouseDraggedPre(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		return false;
	}
	protected boolean mouseDraggedPost(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		return false;
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		mouseX -= x;
		mouseY -= y;
		return mouseScrolledPre(mouseX, mouseY,horizontalAmount, verticalAmount) ||
				super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount) ||
				mouseScrolledPost(mouseX, mouseY, horizontalAmount, verticalAmount);
	}
	protected boolean mouseScrolledPre(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		return false;
	}
	protected boolean mouseScrolledPost(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		return false;
	}
	
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		mouseX -= x;
		mouseY -= y;
		return isMouseOverPre(mouseX, mouseY) ||
				super.isMouseOver(mouseX, mouseY);
	}
	protected boolean isMouseOverPre(double mouseX, double mouseY) {
		return false;
	}
	
}
