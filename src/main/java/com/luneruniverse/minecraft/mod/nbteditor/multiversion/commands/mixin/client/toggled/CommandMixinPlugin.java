package com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.mixin.client.toggled;

import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.misc.BasicMixinPlugin;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

public class CommandMixinPlugin extends BasicMixinPlugin {
	
	@Override
	public void addMixins(List<String> output) {
		Version.newSwitch()
				.range("1.19.0", null, () -> output.add("client.toggled.CommandExecutionC2SPacketMixin"))
				.range(null, "1.18.2", () -> {})
				.run();
	}
	
}
