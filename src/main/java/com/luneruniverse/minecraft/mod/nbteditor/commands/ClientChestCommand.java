package com.luneruniverse.minecraft.mod.nbteditor.commands;

import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ClientChestScreen;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

public class ClientChestCommand extends ClientCommand {
	
	@Override
	public String getName() {
		return "clientchest";
	}
	
	@Override
	public List<String> getAliases() {
		return List.of("chest", "storage");
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
		builder.executes(context -> {
			ClientChestScreen.show();
			return Command.SINGLE_SUCCESS;
		});
	}
	
}
