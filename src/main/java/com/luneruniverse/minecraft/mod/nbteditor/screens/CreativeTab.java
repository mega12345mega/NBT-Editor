package com.luneruniverse.minecraft.mod.nbteditor.screens;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IdentifierInst;
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
	public static final int HEIGHT = 32;
	
	private static final Identifier TEXTURE_TOP;
	private static final Identifier TEXTURE_BOTTOM;
	private static final int V_TOP;
	private static final int V_BOTTOM;
	static {
		if (Version.<Boolean>newSwitch()
				.range("1.20.2", null, true)
				.range(null, "1.20.1", false)
				.get()) {
			TEXTURE_TOP = IdentifierInst.of("nbteditor", "textures/gui/sprites/container/creative_inventory/tab_top_unselected.png");
			TEXTURE_BOTTOM = IdentifierInst.of("nbteditor", "textures/gui/sprites/container/creative_inventory/tab_bottom_unselected.png");
			V_TOP = 0;
			V_BOTTOM = 0;
		} else {
			TEXTURE_TOP = IdentifierInst.of("textures/gui/container/creative_inventory/tabs.png");
			TEXTURE_BOTTOM = IdentifierInst.of("textures/gui/container/creative_inventory/tabs.png");
			V_TOP = 0;
			V_BOTTOM = 64;
		}
	}
	
	private final boolean bottom;
	private final int x;
	private final int y;
	private final ItemStack item;
	private final Runnable onClick;
	
	public CreativeTab(boolean bottom, int x, int y, ItemStack item, Runnable onClick) {
		this.bottom = bottom;
		this.x = x;
		this.y = y;
		this.item = item;
		this.onClick = onClick;
	}
	
	private void renderTab(MatrixStack matrices) {
		MVDrawableHelper.drawTexture(matrices, bottom ? TEXTURE_BOTTOM : TEXTURE_TOP, x, y + (bottom ? 0 : 2), 0, bottom ? V_BOTTOM : V_TOP, WIDTH, 32);
		
		int xOffset = Version.<Integer>newSwitch()
				.range("1.19.3", null, 5)
				.range(null, "1.19.2", 6)
				.get();
		MVDrawableHelper.renderItem(matrices, 100.0F, false, item, x + xOffset, y + (bottom ? 5 : 11));
	}
	private void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
		if (isHoveringOverTab(x, y, mouseX, mouseY))
			MVDrawableHelper.renderTooltip(matrices, item.getName(), mouseX, mouseY);
	}
	
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
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
