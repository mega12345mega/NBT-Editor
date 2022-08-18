package com.luneruniverse.minecraft.mod.nbteditor.commands.factories;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.commands.SubCommand;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;

public class FactoryCommand implements ClientCommand {
	
	@Override
	public LiteralCommandNode<FabricClientCommandSource> register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess cmdReg) {
		return dispatcher.register(SubCommand.registerAll(literal("itemfactory"), cmdReg,
				new AttributesCommand(),
				new BlockStatesCommand(),
				new EnchantmentsCommand(),
				new HideFlagsCommand(),
				new LoreCommand(),
				new MaxCommand(),
				new UnbindSkullCommand()));
	}
	
}
