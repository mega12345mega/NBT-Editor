package com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.hideflags;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;

import net.minecraft.text.Text;

public class TooltipHideFlag extends HideFlag {
	
	public static final HideFlag INSTANCE = new TooltipHideFlag();
	
	private static final Text NAME = TextInst.translatable("nbteditor.hide_flags.tooltip");
	
	private TooltipHideFlag() {}
	
	@Override
	public Text getName() {
		return NAME;
	}
	
}
