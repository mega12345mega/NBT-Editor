package com.luneruniverse.minecraft.mod.nbteditor.screens;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.util.math.MatrixStack;

public class List2D implements Drawable, Element, Selectable {
	
	public static abstract class List2DValue implements Drawable, Element {
		
		protected static final MinecraftClient client = MinecraftClient.getInstance();
		protected static final TextRenderer textRenderer = client.textRenderer;
		
		private boolean insideList;
		
		private void setInsideList(boolean insideList) {
			this.insideList = insideList;
		}
		protected boolean isInsideList() {
			return this.insideList;
		}
		
	}
	
	
	
	private int x;
	private int y;
	private int width;
	private int height;
	private int padding;
	private int itemWidth;
	private int itemHeight;
	private int itemPadding;
	private int scroll;
	
	private final Map<List2DValue, Point> elements;
	private Element finalEventHandler;
	
	public List2D(int x, int y, int width, int height, int padding, int itemWidth, int itemHeight, int itemPadding) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.padding = padding;
		this.itemWidth = itemWidth;
		this.itemHeight = itemHeight;
		this.itemPadding = itemPadding;
		
		this.elements = new LinkedHashMap<>();
	}
	public List2D setFinalEventHandler(Element finalEventHandler) {
		this.finalEventHandler = finalEventHandler;
		return this;
	}
	public List2D addElement(List2DValue element) {
		this.elements.put(element, genPoint(elements.size()));
		return this;
	}
	public List2D removeElement(List2DValue element) {
		if (this.elements.remove(element) != null) {
			this.elements.replaceAll(new BiFunction<>() {
				private int i = 0;
				@Override
				public Point apply(List2DValue t, Point u) {
					return genPoint(i++);
				}
			});
		}
		return this;
	}
	public List2D clearElements() {
		this.elements.clear();
		return this;
	}
	public List2D addElements(List<List2DValue> elements) {
		elements.forEach(this::addElement);
		return this;
	}
	public List<List2DValue> getElements() {
		return new ArrayList<>(this.elements.keySet());
	}
	private Point genPoint(int i) {
		int elementsPerRow = (width + itemPadding) / (itemWidth + itemPadding);
		return new Point(i % elementsPerRow * (itemWidth + itemPadding) + x, i / elementsPerRow * (itemHeight + itemPadding) + y);
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		boolean hovering = isMouseOver(mouseX, mouseY);
		
		MinecraftClient client = MainUtil.client;
		RenderSystem.enableScissor((int) ((x - padding) * client.getWindow().getScaleFactor()), client.getWindow().getHeight() - (int) ((y + height + padding) * client.getWindow().getScaleFactor()), (int) ((width + padding * 2) * client.getWindow().getScaleFactor()), (int) ((height + padding * 2) * client.getWindow().getScaleFactor()));
		for (Map.Entry<List2DValue, Point> element : elements.entrySet()) {
			Point pos = element.getValue();
			matrices.push();
			matrices.translate(pos.x, pos.y + scroll, 0);
			element.getKey().setInsideList(hovering);
			element.getKey().render(matrices, mouseX - pos.x, mouseY - pos.y - scroll, delta);
			matrices.pop();
		}
		RenderSystem.disableScissor();
	}
	
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return mouseX >= x - padding && mouseX <= x + width + padding && mouseY >= y - padding && mouseY <= y + height + padding;
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		boolean hovering = isMouseOver(mouseX, mouseY);
		
		boolean success = false;
		for (Map.Entry<List2DValue, Point> element : new LinkedHashMap<>(this.elements).entrySet()) {
			element.getKey().setInsideList(hovering);
			if (element.getKey().mouseClicked(mouseX - element.getValue().x, mouseY - element.getValue().y - scroll, button))
				success = true;
		}
		if (!success)
			success = finalEventHandler.mouseClicked(mouseX - x, mouseY - y, button);
		return success;
	}
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		boolean hovering = isMouseOver(mouseX, mouseY);
		
		boolean success = false;
		for (Map.Entry<List2DValue, Point> element : this.elements.entrySet()) {
			element.getKey().setInsideList(hovering);
			if (element.getKey().mouseReleased(mouseX - element.getValue().x, mouseY - element.getValue().y - scroll, button))
				success = true;
		}
		if (!success)
			success = finalEventHandler.mouseReleased(mouseX - x, mouseY - y, button);
		return success;
	}
	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		boolean hovering = isMouseOver(mouseX, mouseY);
		
		for (Map.Entry<List2DValue, Point> element : this.elements.entrySet()) {
			element.getKey().setInsideList(hovering);
			element.getKey().mouseMoved(mouseX - element.getValue().x, mouseY - element.getValue().y - scroll);
		}
	}
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		boolean hovering = isMouseOver(mouseX, mouseY);
		
		boolean success = false;
		for (Map.Entry<List2DValue, Point> element : this.elements.entrySet()) {
			element.getKey().setInsideList(hovering);
			if (element.getKey().mouseDragged(mouseX - element.getValue().x, mouseY - element.getValue().y - scroll, button, deltaX, deltaY))
				success = true;
		}
		if (!success)
			success = finalEventHandler.mouseDragged(mouseX - x, mouseY - y, button, deltaX, deltaY);
		return success;
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		boolean hovering = isMouseOver(mouseX, mouseY);
		
		boolean success = false;
		for (Map.Entry<List2DValue, Point> element : this.elements.entrySet()) {
			element.getKey().setInsideList(hovering);
			if (element.getKey().mouseScrolled(mouseX - element.getValue().x, mouseY - element.getValue().y, amount))
				success = true;
		}
		if (!success) {
			int maxScroll = getMaxScroll();
			if (amount < 0 && scroll > maxScroll) {
				success = true;
				scroll += amount * 5;
				if (scroll < maxScroll)
					scroll = maxScroll;
			}
			if (amount > 0 && scroll < 0) {
				success = true;
				scroll += amount * 5;
				if (scroll > 0)
					scroll = 0;
			}
		}
		if (!success)
			success = finalEventHandler.mouseScrolled(mouseX - x, mouseY - y, amount);
		return success;
	}
	public int getMaxScroll() {
		return Math.min(0, elements.isEmpty() ? 0 : (y + height) - genPoint(elements.size() - 1).y - itemHeight);
	}
	public void setScroll(int scroll) {
		this.scroll = scroll;
	}
	public int getScroll() {
		return scroll;
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		boolean success = false;
		for (Map.Entry<List2DValue, Point> element : this.elements.entrySet()) {
			if (element.getKey().keyPressed(keyCode, scanCode, modifiers))
				success = true;
		}
		if (!success)
			success = finalEventHandler.keyPressed(keyCode, scanCode, modifiers);
		return success;
	}
	@Override
	public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		boolean success = false;
		for (Map.Entry<List2DValue, Point> element : this.elements.entrySet()) {
			if (element.getKey().keyReleased(keyCode, scanCode, modifiers))
				success = true;
		}
		if (!success)
			success = finalEventHandler.keyReleased(keyCode, scanCode, modifiers);
		return success;
	}
	
	@Override
	public void appendNarrations(NarrationMessageBuilder builder) {
		
	}
	
	@Override
	public SelectionType getType() {
		return SelectionType.FOCUSED;
	}
	
}
