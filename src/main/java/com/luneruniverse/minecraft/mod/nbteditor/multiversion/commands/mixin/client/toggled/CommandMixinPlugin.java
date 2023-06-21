package com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.mixin.client.toggled;

import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.misc.BasicMixinPlugin;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

public class CommandMixinPlugin extends BasicMixinPlugin {
	
	@Override
	public void addMixins(List<String> output) {
		switch (Version.get()) {
			case v1_19_4, v1_19_3, v1_19 -> output.add("client.toggled.CommandExecutionC2SPacketMixin");
			case v1_18_v1_17 -> {}
		}
	}
	
}
