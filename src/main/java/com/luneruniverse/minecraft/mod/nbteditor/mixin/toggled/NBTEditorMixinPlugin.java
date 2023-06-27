package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.misc.BasicMixinPlugin;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

public class NBTEditorMixinPlugin extends BasicMixinPlugin {
	
	@Override
	public void addMixins(List<String> output) {
		Version.newSwitch()
				.range("1.20.0", null, () -> output.add("toggled.DrawContextMixin"))
				.range("1.19.3", "1.19.4", () -> output.add("toggled.ScreenMixin"))
				.range(null, "1.19.2", () -> {})
				.run();
	}
	
}
