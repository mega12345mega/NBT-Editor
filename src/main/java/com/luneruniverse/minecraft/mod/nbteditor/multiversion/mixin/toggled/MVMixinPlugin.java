package com.luneruniverse.minecraft.mod.nbteditor.multiversion.mixin.toggled;

import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.misc.BasicMixinPlugin;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

public class MVMixinPlugin extends BasicMixinPlugin {
	
	@Override
	public void addMixins(List<String> output) {
		Version.newSwitch()
				.range("1.20.3", null, () -> {})
				.range(null, "1.20.2", () -> output.add("toggled.TextSerializerMixin"))
				.run();
		Version.newSwitch()
				.range("1.20.5", null, () -> output.add("toggled.ItemStackMixin"))
				.range(null, "1.20.4", () -> {})
				.run();
		
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER)
			return;
		
		Version.newSwitch()
				.range("1.19.3", null, () -> output.add("toggled.ScreenMixin"))
				.range(null, "1.19.2", () -> {})
				.run();
		Version.newSwitch()
				.range("1.20.2", null, () -> {})
				.range(null, "1.20.1", () -> output.add("toggled.ElementMixin"))
				.run();
		Version.newSwitch()
				.range("1.20.5", null, () -> output.add("toggled.BookScreenContentsMixin"))
				.range(null, "1.20.4", () -> {})
				.run();
	}
	
}
