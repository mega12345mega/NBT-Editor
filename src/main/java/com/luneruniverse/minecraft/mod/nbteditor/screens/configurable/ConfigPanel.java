package com.luneruniverse.minecraft.mod.nbteditor.screens.configurable;

import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVTooltip;
import com.luneruniverse.minecraft.mod.nbteditor.screens.Tickable;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.Panel;

import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.util.math.MatrixStack;

public class ConfigPanel extends Panel<ConfigPath> implements Tickable {
	
	private final ConfigPath toRender;
	private final List<PositionedPanelElement<ConfigPath>> elements;
	
	public ConfigPanel(int x, int y, int width, int height, ConfigPath toRender) {
		super(x, y, width, height, 1, true);
		this.toRender = toRender;
		this.elements = List.of(new PositionedPanelElement<>(toRender, 0, 0));
	}
	
	public void setScroll(int scroll) {
		this.scroll = scroll;
	}
	public int getScroll() {
		return scroll;
	}
	
	@Override
	protected Iterable<PositionedPanelElement<ConfigPath>> getPanelElements() {
		return elements;
	}
	
	@Override
	protected int getHighestY() {
		return toRender.getRenderHeight();
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		boolean prevOneTooltip = MVTooltip.isOneTooltip();
		if (!prevOneTooltip)
			MVTooltip.setOneTooltip(true, true);
		try {
			super.render(matrices, mouseX, mouseY, delta);
		} finally {
			if (!prevOneTooltip)
				MVTooltip.renderOneTooltip(matrices, mouseX, mouseY);
		}
	}
	
	@Override
	public void tick() {
		toRender.tick();
	}
	
	
	
	@Override
	public SelectionType getType() {
		return SelectionType.NONE;
	}
	
	@Override
	public void appendNarrations(NarrationMessageBuilder builder) {
		
	}
	
}
