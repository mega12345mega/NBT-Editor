package com.luneruniverse.minecraft.mod.nbteditor.commands;

import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.literal;

import java.util.Arrays;
import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.screens.ClientChestScreen;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.minecraft.command.CommandSource;

public class ClientChestCommand implements RegisterableCommand {
	
	@Override
	public LiteralCommandNode<? extends CommandSource> register(boolean dedicated) {
		return ClientCommandManager.DISPATCHER.register(literal("clientchest").executes(context -> {
			ClientChestScreen.show();
			return Command.SINGLE_SUCCESS;
		}));
	}
	
	@Override
	public List<String> getAliases() {
		return Arrays.asList("chest", "storage");
	}
	
	@Override
	public EnvType getSide() {
		return EnvType.CLIENT;
	}
	
}
