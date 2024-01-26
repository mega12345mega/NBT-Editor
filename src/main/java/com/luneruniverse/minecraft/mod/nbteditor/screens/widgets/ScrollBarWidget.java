package com.luneruniverse.minecraft.mod.nbteditor.screens.widgets;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawable;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVElement;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;

import net.minecraft.client.util.math.MatrixStack;

public class ScrollBarWidget implements MVDrawable, MVElement {
	
	private final int x;
	private final int y;
	private final int height;
	private final Supplier<Integer> getScroll;
	private final Consumer<Integer> setScroll;
	private final Supplier<Integer> getMaxScroll;
	private boolean dragging;
	private double dragStartMouseY;
	private int dragStartScroll;
	
	public ScrollBarWidget(int x, int y, int height, Supplier<Integer> getScroll, Consumer<Integer> setScroll, Supplier<Integer> getMaxScroll) {
		this.x = x;
		this.y = y;
		this.height = height;
		this.getScroll = getScroll;
		this.setScroll = setScroll;
		this.getMaxScroll = getMaxScroll;
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		int scroll = getScroll.get();
		
		double maxScroll = -Math.min(scroll - height, getMaxScroll.get() - height);
		double scrollArea = height / maxScroll;
		if (scrollArea < 1) {
			double barY = y - scroll / (maxScroll + scrollArea) * height;
			MVDrawableHelper.fill(matrices, x, y, x + 8, y + height, 0xFFAAAAAA);
			MVDrawableHelper.fill(matrices, x, (int) barY, x + 8, (int) (barY + scrollArea * height + 1), 0xFF000000);
		}
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (mouseX >= x && mouseX <= x + 8) {
			dragging = true;
			dragStartMouseY = mouseY;
			dragStartScroll = getScroll.get();
			return true;
		}
		
		dragging = false;
		return false;
	}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (!dragging)
			return false;
		
		int scroll = getScroll.get();
		double maxScroll = -Math.min(scroll - height, getMaxScroll.get() - height);
		double scrollArea = height / maxScroll;
		double barY = y - dragStartScroll / (maxScroll + scrollArea) * height;
		scroll((y - barY - (mouseY - dragStartMouseY)) * (maxScroll + scrollArea) / height - dragStartScroll, dragStartScroll);
		return true;
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double xAmount, double yAmount) {
		return scroll(yAmount * 5 * ConfigScreen.getScrollSpeed(), getScroll.get());
	}
	
	private boolean scroll(double amount, int scroll) {
		int maxScroll = getMaxScroll.get();
		
		if (amount < 0 && scroll > maxScroll) {
			scroll += amount;
			if (scroll < maxScroll)
				scroll = maxScroll;
			setScroll.accept(scroll);
			return true;
		}
		if (amount > 0 && scroll < 0) {
			scroll += amount;
			if (scroll > 0)
				scroll = 0;
			setScroll.accept(scroll);
			return true;
		}
		setScroll.accept(scroll);
		return false;
	}
	
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return true;
	}
	
}
