package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.misc.BasicMixinPlugin;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;

public class NBTEditorMixinPlugin extends BasicMixinPlugin {
	
	@Override
	public void addMixins(List<String> output) {
		try {
			Reflection.getClass("net.minecraft.class_8000"); // TooltipPositioner
			output.add("toggled.ScreenMixin");
		} catch (RuntimeException e) {}
	}
	
}
