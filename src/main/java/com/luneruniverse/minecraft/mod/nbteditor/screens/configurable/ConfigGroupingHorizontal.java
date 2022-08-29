package com.luneruniverse.minecraft.mod.nbteditor.screens.configurable;

import java.util.ArrayList;

import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public abstract class ConfigGroupingHorizontal<K, T extends ConfigGroupingHorizontal<K, T>> extends ConfigGrouping<K, T> {
	
	protected ConfigGroupingHorizontal(Text name, Constructor<K, T> cloneImpl) {
		super(name, cloneImpl);
	}
	
	protected int getNameWidth() {
		return name == null ? 0 : MainUtil.client.textRenderer.getWidth(name) + PADDING;
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		int xOffset = 0;
		Text fullName = getFullName();
		if (fullName != null) {
			DrawableHelper.drawTextWithShadow(matrices, MainUtil.client.textRenderer, fullName, PADDING * 2, 0, 0xFFFFFFFF);
			xOffset += getNameWidth();
		}
		
		for (ConfigPath path : new ArrayList<>(paths.values())) {
			matrices.push();
			matrices.translate(xOffset, 0, 0);
			path.render(matrices, mouseX - xOffset, mouseY, delta);
			matrices.pop();
			
			xOffset += path.getSpacingWidth() + PADDING;
		}
	}
	
	@Override
	public int getSpacingWidth() {
		return getNameWidth() + paths.values().stream().mapToInt(Configurable::getSpacingWidth).reduce((a, b) -> a + PADDING + b).orElse(0);
	}
	
	@Override
	public int getSpacingHeight() {
		int output = 0;
		for (ConfigPath path : paths.values()) {
			int height = path.getSpacingHeight();
			if (height > output)
				output = height;
		}
		return output;
	}
	
	@Override
	public int getRenderWidth() {
		int output = getNameWidth();
		int xOffset = output;
		for (ConfigPath path : paths.values()) {
			int rightX = xOffset + path.getRenderWidth();
			if (rightX > output)
				output = rightX;
			xOffset += path.getSpacingWidth() + PADDING;
		}
		return output;
	}
	
	@Override
	public int getRenderHeight() {
		int output = 0;
		for (ConfigPath path : paths.values()) {
			int height = path.getRenderHeight();
			if (height > output)
				output = height;
		}
		return output;
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		int xOffset = getNameWidth();
		
		for (ConfigPath path : new ArrayList<>(paths.values())) {
			if (path.mouseClicked(mouseX - xOffset, mouseY, button))
				return true;
			xOffset += path.getSpacingWidth() + PADDING;
		}
		return false;
	}
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		int xOffset = getNameWidth();
		
		for (ConfigPath path : new ArrayList<>(paths.values())) {
			if (path.mouseReleased(mouseX - xOffset, mouseY, button))
				return true;
			xOffset += path.getSpacingWidth() + PADDING;
		}
		return false;
	}
	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		int xOffset = getNameWidth();
		
		for (ConfigPath path : new ArrayList<>(paths.values())) {
			path.mouseMoved(mouseX - xOffset, mouseY);
			xOffset += path.getSpacingWidth() + PADDING;
		}
	}
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		int xOffset = getNameWidth();
		
		for (ConfigPath path : new ArrayList<>(paths.values())) {
			if (path.mouseDragged(mouseX - xOffset, mouseY, button, deltaX, deltaY))
				return true;
			xOffset += path.getSpacingWidth() + PADDING;
		}
		return false;
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		int xOffset = getNameWidth();
		
		for (ConfigPath path : new ArrayList<>(paths.values())) {
			if (path.mouseScrolled(mouseX - xOffset, mouseY, amount))
				return true;
			xOffset += path.getSpacingWidth() + PADDING;
		}
		return false;
	}
	
}
