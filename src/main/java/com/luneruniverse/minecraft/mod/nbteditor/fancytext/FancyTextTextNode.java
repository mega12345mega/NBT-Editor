package com.luneruniverse.minecraft.mod.nbteditor.fancytext;

import net.minecraft.text.Style;

public record FancyTextTextNode(String text) implements FancyTextNode {
	
	@Override
	public Style modifyStyle(Style style) {
		return style;
	}
	
	@Override
	public int getNumberOfTextNodes() {
		return 1;
	}
	
}
