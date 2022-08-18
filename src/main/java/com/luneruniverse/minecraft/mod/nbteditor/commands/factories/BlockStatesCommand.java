package com.luneruniverse.minecraft.mod.nbteditor.commands.factories;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import com.luneruniverse.minecraft.mod.nbteditor.commands.SubCommand;
import com.luneruniverse.minecraft.mod.nbteditor.screens.BlockStatesScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.BlockItem;
import net.minecraft.text.Text;

public class BlockStatesCommand implements SubCommand {
	
	@Override
	public LiteralArgumentBuilder<FabricClientCommandSource> register(LiteralArgumentBuilder<FabricClientCommandSource> parent, CommandRegistryAccess cmdReg) {
		return parent.then(literal("blockstates").executes(context -> {
			MainUtil.client.setScreen(new BlockStatesScreen(MainUtil.getHeldItem(item -> item.getItem() instanceof BlockItem, Text.translatable("nbteditor.noblock"))));
			return Command.SINGLE_SUCCESS;
		}));
	}
	
}
