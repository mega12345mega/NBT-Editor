package com.luneruniverse.minecraft.mod.nbteditor.commands;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.ClientChestScreen;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

public class ClientChestCommand extends ClientCommand {
	
	public static final ClientChestCommand INSTANCE = new ClientChestCommand();
	
	private ClientChestCommand() {
		
	}
	
	@Override
	public String getName() {
		return "clientchest";
	}
	
	@Override
	public String getExtremeAlias() {
		return "cc";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder, String path) {
		builder.executes(context -> {
			ClientChestScreen.show();
			return Command.SINGLE_SUCCESS;
		});
	}
	
}
