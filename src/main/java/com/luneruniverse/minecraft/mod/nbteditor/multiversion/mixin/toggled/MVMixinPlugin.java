package com.luneruniverse.minecraft.mod.nbteditor.multiversion.mixin.toggled;

import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.misc.BasicMixinPlugin;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

public class MVMixinPlugin extends BasicMixinPlugin {
	
	@Override
	public void addMixins(List<String> output) {
		Version.newSwitch()
				.range("1.19.3", null, () -> output.add("toggled.ScreenMixin"))
				.range(null, "1.19.2", () -> {})
				.run();
	}
	
}
