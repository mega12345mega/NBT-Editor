package com.luneruniverse.minecraft.mod.nbteditor.commands;

import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.literal;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

public abstract class ClientCommand {
	
	public static <T> T getDefaultArg(CommandContext<FabricClientCommandSource> context, String name, T defaultValue, Class<T> type) {
		try {
			return context.getArgument(name, type);
		} catch (IllegalArgumentException e) {
			return defaultValue;
		}
	}
	
	
	public ClientCommand() {
		
	}
	
	public void registerAll(Consumer<LiteralArgumentBuilder<FabricClientCommandSource>> commandHandler, String path) {
		LiteralArgumentBuilder<FabricClientCommandSource> builder = literal(getName());
		register(builder, path);
		commandHandler.accept(builder);
		
		Set<String> aliases = new HashSet<>();
		for (ConfigScreen.Alias alias : ConfigScreen.getAliases()) {
			if (alias.original().equals(path))
				aliases.add(alias.alias());
		}
		for (String alias : aliases) {
			builder = literal(alias);
			register(builder, path);
			commandHandler.accept(builder);
		}
	}
	
	public abstract String getName();
	public abstract String getExtremeAlias();
	public abstract void register(LiteralArgumentBuilder<FabricClientCommandSource> builder, String path);
	public ClientCommand getShortcut(List<String> path, int index) {
		if (path.size() == index)
			return this;
		return null;
	}
	
}
