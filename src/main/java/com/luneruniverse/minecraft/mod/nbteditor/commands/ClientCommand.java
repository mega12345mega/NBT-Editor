package com.luneruniverse.minecraft.mod.nbteditor.commands;

import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.literal;

import java.util.List;
import java.util.function.Consumer;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
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
	
	public void registerAll(Consumer<LiteralArgumentBuilder<FabricClientCommandSource>> commandHandler) {
		LiteralArgumentBuilder<FabricClientCommandSource> builder = literal(getName());
		register(builder);
		commandHandler.accept(builder);
		
		List<String> aliases = getAliases();
		if (aliases != null) {
			for (String alias : aliases) {
				builder = literal(alias);
				register(builder);
				commandHandler.accept(builder);
			}
		}
	}
	
	public abstract String getName();
	public List<String> getAliases() {
		return null;
	}
	public abstract void register(LiteralArgumentBuilder<FabricClientCommandSource> builder);
	public ClientCommand getShortcut(List<String> path, int index) {
		if (path.size() == index)
			return this;
		return null;
	}
	
}
