package com.luneruniverse.minecraft.mod.nbteditor.screens;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class CreativeTab {
	
	public static class CreativeTabGroup implements Element, Drawable, Selectable {
		private final List<CreativeTab> tabs;
		
		public CreativeTabGroup(List<CreativeTab> tabs) {
			this.tabs = tabs;
		}
		
		@Override
		public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
			for (CreativeTab tab : tabs)
				tab.renderTab(matrices);
			for (CreativeTab tab : tabs)
				tab.renderTooltip(matrices, mouseX, mouseY);
		}
		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			for (CreativeTab tab : tabs) {
				if (tab.mouseClicked(mouseX, mouseY, button))
					return true;
			}
			return false;
		}
		
		@Override
		public SelectionType getType() {
			return SelectionType.NONE;
		}
		@Override
		public void appendNarrations(NarrationMessageBuilder builder) {
			
		}
	}
	
	
	public static record CreativeTabData(ItemStack item, Runnable onClick, Predicate<Screen> whenToShow) {}
	public static final List<CreativeTabData> TABS = new ArrayList<>();
	public static final int WIDTH = switch (Version.get()) {
		case v1_19_3 -> 26;
		case v1_19, v1_18 -> 28;
	};
	
	private static final Identifier tabs = new Identifier("textures/gui/container/creative_inventory/tabs.png");
	
	private final Screen screen;
	private final int x;
	private final ItemStack item;
	private final Runnable onClick;
	
	public CreativeTab(Screen screen, int x, ItemStack item, Runnable onClick) {
		this.screen = screen;
		this.x = x;
		this.item = item;
		this.onClick = onClick;
	}
	
	private void renderTab(MatrixStack matrices) {
		ItemRenderer itemRenderer = MainUtil.client.getItemRenderer();
		TextRenderer textRenderer = MainUtil.client.textRenderer;
		
		int j = 0;
		int k = 0;
		int y = screen.height - 32;
		
		RenderSystem.setShader(GameRenderer::getPositionTexProgram); // getPositionTexShader <= 1.19.2
		RenderSystem.setShaderTexture(0, tabs);
		screen.drawTexture(matrices, x, y, j, k, WIDTH, 32);
		itemRenderer.zOffset = 100.0F;
		
		int xOffset = switch (Version.get()) {
			case v1_19_3 -> 5;
			case v1_19, v1_18 -> 6;
		};
		itemRenderer.renderInGuiWithOverrides(item, x + xOffset, y + 9);
		itemRenderer.renderGuiItemOverlay(textRenderer, item, x + xOffset, y + 9);
		itemRenderer.zOffset = 0.0F;
	}
	private void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
		int y = screen.height - 32;
		
		if (isHoveringOverTab(x, y, mouseX, mouseY))
			screen.renderTooltip(matrices, item.getName(), mouseX, mouseY);
	}
	
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		int y = screen.height - 32;
		
		if (isHoveringOverTab(x, y, (int) mouseX, (int) mouseY)) {
			onClick.run();
			return true;
		}
		
		return false;
	}
	
	private boolean isHoveringOverTab(int x, int y, int mouseX, int mouseY) {
		return mouseX >= x + 3 && mouseX <= x + 3 + 23 && mouseY >= y + 3 && mouseY <= y + 3 + 27;
	}
	
}
