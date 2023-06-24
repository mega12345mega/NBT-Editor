package com.luneruniverse.minecraft.mod.nbteditor.commands;

import com.luneruniverse.minecraft.mod.nbteditor.containers.ContainerIO;
import com.luneruniverse.minecraft.mod.nbteditor.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.ContainerScreen;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

public class OpenCommand extends ClientCommand {
	
	public static final OpenCommand INSTANCE = new OpenCommand();
	
	private OpenCommand() {
		
	}
	
	@Override
	public String getName() {
		return "open";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
		builder.executes(context -> {
			ContainerScreen.show(ItemReference.getHeldItem(ContainerIO::isContainer, TextInst.translatable("nbteditor.no_hand.no_item.container")));
			return Command.SINGLE_SUCCESS;
		});
	}
	
}
