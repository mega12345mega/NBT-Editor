package com.luneruniverse.minecraft.mod.nbteditor.commands;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import java.util.Arrays;
import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.containers.ContainerIO;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ItemsScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;

public class ItemsCommand implements ClientCommand {
	
	@Override
	public LiteralCommandNode<FabricClientCommandSource> register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess cmdReg) {
		return dispatcher.register(literal("items").executes(context -> {
			ItemsScreen.show(MainUtil.getHeldItem(ContainerIO::isContainer, Text.translatable("nbteditor.nocontainer")));
			return Command.SINGLE_SUCCESS;
		}));
	}
	
	@Override
	public List<String> getAliases() {
		return Arrays.asList("open");
	}
	
}
