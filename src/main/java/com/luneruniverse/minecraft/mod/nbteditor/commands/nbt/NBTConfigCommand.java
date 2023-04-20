package com.luneruniverse.minecraft.mod.nbteditor.commands.nbt;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

public class NBTConfigCommand extends ClientCommand {
	
	@Override
	public String getName() {
		return "config";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
		builder.executes(context -> {
			MainUtil.client.setScreen(new ConfigScreen(null));
			return Command.SINGLE_SUCCESS;
		});
	}
	
}
