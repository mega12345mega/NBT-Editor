package com.luneruniverse.minecraft.mod.nbteditor.screens.configurable;

import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public interface ConfigTooltipSupplier {
	public List<Text> getTooltip();
	public default void render(MatrixStack matrices, int x, int y) {
		MainUtil.client.currentScreen.renderTooltip(matrices, getTooltip(), x, y);
	}
}
