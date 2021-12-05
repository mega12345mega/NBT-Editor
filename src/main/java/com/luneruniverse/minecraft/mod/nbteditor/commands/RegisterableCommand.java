package com.luneruniverse.minecraft.mod.nbteditor.commands;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;

public interface RegisterableCommand {
	@SuppressWarnings("unchecked")
	public default void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
		LiteralCommandNode<? extends CommandSource> node = register(dedicated);
		if (node == null)
			return;
		getAliases().forEach(alias -> {
			try {
				if (getSide() == EnvType.CLIENT) {
					ClientCommandManager.DISPATCHER.register(buildRedirect(alias, (LiteralCommandNode<FabricClientCommandSource>) node));
				} else {
					dispatcher.register(buildRedirect(alias, (LiteralCommandNode<ServerCommandSource>) node));
				}
			} catch (ClassCastException e) {
				e.printStackTrace();
			}
		});
	}
	
	/**
	 * From: https://github.com/VelocityPowered/Velocity/blob/8abc9c80a69158ebae0121fda78b55c865c0abad/proxy/src/main/java/com/velocitypowered/proxy/util/BrigadierUtils.java#L38
	 * Edited to support generics & return the builder
	 * 
	 * Returns a literal node that redirects its execution to the given destination
	 * node.
	 *
	 * @param alias       the command alias
	 * @param destination the destination node
	 * @return the built node
	 */
	public static <T extends CommandSource> LiteralArgumentBuilder<T> buildRedirect(final String alias,
			final LiteralCommandNode<T> destination) {
		// Redirects only work for nodes with children, but break the top argument-less
		// command.
		// Manually adding the root command after setting the redirect doesn't fix it.
		// See https://github.com/Mojang/brigadier/issues/46). Manually clone the node
		// instead.
		LiteralArgumentBuilder<T> builder = LiteralArgumentBuilder
				.<T>literal(alias).requires(destination.getRequirement())
				.forward(destination.getRedirect(), destination.getRedirectModifier(), destination.isFork())
				.executes(destination.getCommand());
		for (CommandNode<T> child : destination.getChildren()) {
			builder.then(child);
		}
		return builder;
	}
	
	public LiteralCommandNode<? extends CommandSource> register(boolean dedicated);
	public default List<String> getAliases() {
		return new ArrayList<>();
	}
	public default EnvType getSide() {
		return null;
	}
}
