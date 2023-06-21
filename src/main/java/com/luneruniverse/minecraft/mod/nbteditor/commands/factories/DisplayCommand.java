package com.luneruniverse.minecraft.mod.nbteditor.commands.factories;

import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommandGroup;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.screens.factories.DisplayScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

public class DisplayCommand extends ClientCommandGroup {
	
	public DisplayCommand() {
		super(List.of(
				new HideFlagsCommand(),
				new LoreCommand(),
				new NameCommand()));
	}
	
	@Override
	public String getName() {
		return "display";
	}
	
	@Override
	public boolean allowShortcuts() {
		return true;
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
		super.register(builder);
		builder.executes(context -> {
			MainUtil.client.setScreen(new DisplayScreen(MainUtil.getHeldItem()));
			return Command.SINGLE_SUCCESS;
		});
	}
	
}
