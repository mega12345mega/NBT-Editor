package com.luneruniverse.minecraft.mod.nbteditor.fancytext;

import net.minecraft.text.Style;

public record FancyTextShadowColorNode(int color) implements FancyTextNode {
	
	@Override
	public Style modifyStyle(Style style) {
		return style.withShadowColor(color);
	}
	
	@Override
	public int getNumberOfTextNodes() {
		return 0;
	}
	
}
