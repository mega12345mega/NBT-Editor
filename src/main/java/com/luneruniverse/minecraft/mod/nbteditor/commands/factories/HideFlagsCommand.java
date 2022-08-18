package com.luneruniverse.minecraft.mod.nbteditor.commands.factories;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import com.luneruniverse.minecraft.mod.nbteditor.commands.SubCommand;
import com.luneruniverse.minecraft.mod.nbteditor.screens.HideFlagsScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;

public class HideFlagsCommand implements SubCommand {
	
	@Override
	public LiteralArgumentBuilder<FabricClientCommandSource> register(LiteralArgumentBuilder<FabricClientCommandSource> parent, CommandRegistryAccess cmdReg) {
		return parent.then(literal("hideflags").executes(context -> {
			MainUtil.client.setScreen(new HideFlagsScreen(MainUtil.getHeldItem()));
			return Command.SINGLE_SUCCESS;
		}));
	}
	
}
