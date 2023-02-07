package com.luneruniverse.minecraft.mod.nbteditor.commands;

import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.containers.ContainerIO;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ItemsScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

public class ItemsCommand extends ClientCommand {
	
	public static final ItemsCommand INSTANCE = new ItemsCommand();
	
	private ItemsCommand() {
		
	}
	
	@Override
	public String getName() {
		return "items";
	}
	
	@Override
	public List<String> getAliases() {
		return List.of("open");
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
		builder.executes(context -> {
			ItemsScreen.show(MainUtil.getHeldItem(ContainerIO::isContainer, TextInst.translatable("nbteditor.no_hand.no_item.container")));
			return Command.SINGLE_SUCCESS;
		});
	}
	
}
