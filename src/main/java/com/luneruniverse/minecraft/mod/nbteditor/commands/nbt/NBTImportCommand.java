package com.luneruniverse.minecraft.mod.nbteditor.commands.nbt;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ImportScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

public class NBTImportCommand extends ClientCommand {
	
	@Override
	public String getName() {
		return "import";
	}
	
	@Override
	public String getExtremeAlias() {
		return "i";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder, String path) {
		builder.executes(context -> {
			MainUtil.client.setScreen(new ImportScreen());
			return Command.SINGLE_SUCCESS;
		});
	}
	
}
