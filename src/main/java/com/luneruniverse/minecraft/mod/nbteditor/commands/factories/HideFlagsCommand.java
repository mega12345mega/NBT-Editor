package com.luneruniverse.minecraft.mod.nbteditor.commands.factories;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.screens.factories.HideFlagsScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

public class HideFlagsCommand extends ClientCommand {
	
	@Override
	public String getName() {
		return "hideflags";
	}
	
	@Override
	public String getExtremeAlias() {
		return "hf";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder, String path) {
		builder.executes(context -> {
			MainUtil.client.setScreen(new HideFlagsScreen(ItemReference.getHeldItem()));
			return Command.SINGLE_SUCCESS;
		});
	}
	
}
