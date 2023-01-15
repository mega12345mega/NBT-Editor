package com.luneruniverse.minecraft.mod.nbteditor.screens;

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

public class CreativeTab implements Element, Drawable, Selectable {
	
	private static final Identifier tabs = new Identifier("textures/gui/container/creative_inventory/tabs.png");
	
	private final Screen screen;
	private final ItemStack item;
	private final Runnable onClick;
	
	public CreativeTab(Screen screen, ItemStack item, Runnable onClick) {
		this.screen = screen;
		this.item = item;
		this.onClick = onClick;
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		ItemRenderer itemRenderer = MainUtil.client.getItemRenderer();
		TextRenderer textRenderer = MainUtil.client.textRenderer;
		
		int j = 0;
		int k = 0;
		int x = 10;
		int y = screen.height - 32;
		
		RenderSystem.setShader(GameRenderer::getPositionTexProgram); // getPositionTexShader <= 1.19.2
		RenderSystem.setShaderTexture(0, tabs);
		screen.drawTexture(matrices, x, y, j, k, switch (Version.get()) {
			case v1_19_3 -> 26;
			case v1_19, v1_18 -> 28;
		}, 32);
		itemRenderer.zOffset = 100.0F;
		
		int xOffset = switch (Version.get()) {
			case v1_19_3 -> 5;
			case v1_19, v1_18 -> 6;
		};
		itemRenderer.renderInGuiWithOverrides(item, x + xOffset, y + 9);
		itemRenderer.renderGuiItemOverlay(textRenderer, item, x + xOffset, y + 9);
		itemRenderer.zOffset = 0.0F;
		
		if (isHoveringOverTab(x, y, mouseX, mouseY))
			screen.renderTooltip(matrices, item.getName(), mouseX, mouseY);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		int x = 10;
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
	
	
	
	@Override
	public SelectionType getType() {
		return SelectionType.FOCUSED;
	}
	
	@Override
	public void appendNarrations(NarrationMessageBuilder builder) {
		
	}
	
}
