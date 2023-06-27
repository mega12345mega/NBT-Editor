package com.luneruniverse.minecraft.mod.nbteditor.screens;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawable;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVElement;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class CreativeTab {
	
	public static class CreativeTabGroup implements MVDrawable, MVElement, Selectable {
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
	public static final int WIDTH = Version.<Integer>newSwitch()
			.range("1.19.3", null, 26)
			.range(null, "1.19.2", 28)
			.get();
	
	private static final Identifier TEXTURE = new Identifier("textures/gui/container/creative_inventory/tabs.png");
	
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
		int j = 0;
		int k = 0;
		int y = screen.height - 32;
		
		MVDrawableHelper.drawTexture(matrices, TEXTURE, x, y, j, k, WIDTH, 32);
		
		int xOffset = Version.<Integer>newSwitch()
				.range("1.19.3", null, 5)
				.range(null, "1.19.2", 6)
				.get();
		MVDrawableHelper.renderItem(matrices, 100.0F, false, item, x + xOffset, y + 9);
	}
	private void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
		int y = screen.height - 32;
		
		if (isHoveringOverTab(x, y, mouseX, mouseY))
			MVDrawableHelper.renderTooltip(matrices, item.getName(), mouseX, mouseY);
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
