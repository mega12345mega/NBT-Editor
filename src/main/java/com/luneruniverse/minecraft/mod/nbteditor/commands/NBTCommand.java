package com.luneruniverse.minecraft.mod.nbteditor.commands;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import java.util.Arrays;
import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;

public class NBTCommand implements ClientCommand {
	
	@Override
	public LiteralCommandNode<FabricClientCommandSource> register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess cmdReg) {
		return dispatcher.register(literal("nbteditor").then(literal("config").executes(context -> {
			MainUtil.client.setScreen(new ConfigScreen(null));
			return Command.SINGLE_SUCCESS;
		})).executes(context -> {
			MainUtil.client.setScreen(new NBTEditorScreen(MainUtil.getHeldItem()));
			return Command.SINGLE_SUCCESS;
		}));
	}
	
	@Override
	public List<String> getAliases() {
		return Arrays.asList("nbt");
	}
	
}
