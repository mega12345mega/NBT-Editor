package com.luneruniverse.minecraft.mod.nbteditor.commands.factories;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.screens.EnchantmentsScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

public class EnchantmentsCommand extends ClientCommand {
	
	@Override
	public String getName() {
		return "enchantments";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
		builder.executes(context -> {
			MainUtil.client.setScreen(new EnchantmentsScreen(MainUtil.getHeldItem()));
			return Command.SINGLE_SUCCESS;
		});
	}
	
}
