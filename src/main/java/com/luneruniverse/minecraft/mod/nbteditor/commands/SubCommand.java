package com.luneruniverse.minecraft.mod.nbteditor.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;

public interface SubCommand {
	public static LiteralArgumentBuilder<FabricClientCommandSource> registerAll(LiteralArgumentBuilder<FabricClientCommandSource> parent, CommandRegistryAccess cmdReg, SubCommand... subCmds) {
		for (SubCommand subCmd : subCmds)
			parent = subCmd.register(parent, cmdReg);
		return parent;
	}
	
	public LiteralArgumentBuilder<FabricClientCommandSource> register(LiteralArgumentBuilder<FabricClientCommandSource> parent, CommandRegistryAccess cmdReg);
}
