package com.luneruniverse.minecraft.mod.nbteditor.screens.widgets;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IdentifierInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawable;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVElement;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class CreativeTabWidget implements MVDrawable, MVElement {
	
	public static record CreativeTabData(ItemStack item, Runnable onClick, Predicate<Screen> whenToShow) {}
	public static final List<CreativeTabData> TABS = new ArrayList<>();
	
	public static void addCreativeTabs(Screen screen) {
		List<CreativeTabWidget.CreativeTabData> tabs = TABS.stream().filter(tab -> tab.whenToShow().test(screen)).toList();
		if (!tabs.isEmpty()) {
			GroupWidget group = new GroupWidget();
			for (int i = 0; i < tabs.size(); i++) {
				CreativeTabWidget.CreativeTabData tab = tabs.get(i);
				Point pos = ConfigScreen.getCreativeTabsPos().position(i, tabs.size(), screen.width, screen.height);
				group.addWidget(new CreativeTabWidget(ConfigScreen.getCreativeTabsPos().isTop(), pos.x, pos.y, tab.item(), tab.onClick()));
			}
			screen.addDrawableChild(group);
		}
	}
	
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
	
	public CreativeTabWidget(boolean bottom, int x, int y, ItemStack item, Runnable onClick) {
		this.bottom = bottom;
		this.x = x;
		this.y = y;
		this.item = item;
		this.onClick = onClick;
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		MVDrawableHelper.drawTexture(matrices, bottom ? TEXTURE_BOTTOM : TEXTURE_TOP, x, y + (bottom ? 0 : 2), 0, bottom ? V_BOTTOM : V_TOP, WIDTH, 32);
		
		int xOffset = Version.<Integer>newSwitch()
				.range("1.19.3", null, 5)
				.range(null, "1.19.2", 6)
				.get();
		MVDrawableHelper.renderItem(matrices, 100.0F, false, item, x + xOffset, y + (bottom ? 5 : 11));
		
		if (isMouseOver(mouseX, mouseY))
			MVDrawableHelper.renderTooltip(matrices, item.getName(), mouseX, mouseY);
	}
	
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return x <= mouseX && mouseX < x + WIDTH && y <= mouseY && mouseY < y + HEIGHT;
	}
	
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (isMouseOver(mouseX, mouseY)) {
			onClick.run();
			return true;
		}
		
		return false;
	}
	
}
