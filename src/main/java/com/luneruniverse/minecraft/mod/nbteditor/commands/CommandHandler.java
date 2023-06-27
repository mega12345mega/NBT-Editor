package com.luneruniverse.minecraft.mod.nbteditor.commands;

import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.literal;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.luneruniverse.minecraft.mod.nbteditor.commands.factories.FactoryCommand;
import com.luneruniverse.minecraft.mod.nbteditor.commands.get.GetCommand;
import com.luneruniverse.minecraft.mod.nbteditor.commands.nbt.NBTCommand;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

public class CommandHandler {
	
	public static void registerCommands() {
		MVMisc.registerCommands(dispatcher -> {
			for (ClientCommand cmd : COMMANDS.values())
				cmd.registerAll(dispatcher::register);
			
			for (String shortcut : ConfigScreen.getShortcuts()) {
				List<String> path = Arrays.asList(shortcut.split(" "));
				if (path.size() <= 1)
					continue;
				ClientCommand cmd = COMMANDS.get(path.get(0));
				if (cmd == null)
					continue;
				cmd = cmd.getShortcut(path, 1);
				if (cmd != null) {
					LiteralArgumentBuilder<FabricClientCommandSource> builder = literal(path.get(path.size() - 1));
					cmd.register(builder);
					dispatcher.register(builder);
				}
			}
		});
	}
	
	public static final Map<String, ClientCommand> COMMANDS = Stream.of(
			NBTCommand.INSTANCE,
			ClientChestCommand.INSTANCE,
			OpenCommand.INSTANCE,
			GetCommand.INSTANCE,
			FactoryCommand.INSTANCE)
			.collect(Collectors.toUnmodifiableMap(ClientCommand::getName, cmd -> cmd));
	
}
