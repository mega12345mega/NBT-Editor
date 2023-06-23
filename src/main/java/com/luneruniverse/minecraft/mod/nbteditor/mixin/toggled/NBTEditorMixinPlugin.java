package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.misc.BasicMixinPlugin;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

public class NBTEditorMixinPlugin extends BasicMixinPlugin {
	
	@Override
	public void addMixins(List<String> output) {
		switch (Version.get()) {
			case v1_20 -> output.add("toggled.DrawContextMixin");
			case v1_19_4, v1_19_3 -> output.add("toggled.ScreenMixin");
			case v1_19, v1_18_v1_17 -> {}
		}
	}
	
}
