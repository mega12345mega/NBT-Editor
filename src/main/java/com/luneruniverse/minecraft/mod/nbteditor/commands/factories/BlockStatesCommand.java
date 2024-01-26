package com.luneruniverse.minecraft.mod.nbteditor.commands.factories;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.screens.factories.BlockStatesScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.item.BlockItem;

public class BlockStatesCommand extends ClientCommand {
	
	@Override
	public String getName() {
		return "blockstates";
	}
	
	@Override
	public String getExtremeAlias() {
		return "bs";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder, String path) {
		builder.executes(context -> {
			MainUtil.client.setScreen(new BlockStatesScreen(ItemReference.getHeldItem(
					item -> item.getItem() instanceof BlockItem, TextInst.translatable("nbteditor.no_hand.no_item.block"))));
			return Command.SINGLE_SUCCESS;
		});
	}
	
}
