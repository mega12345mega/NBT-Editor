package com.luneruniverse.minecraft.mod.nbteditor.commands.factories;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.NBTReference;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.NBTReferenceFilter;
import com.luneruniverse.minecraft.mod.nbteditor.screens.factories.BlockStatesScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.item.BlockItem;

public class BlockStatesCommand extends ClientCommand {
	
	public static final NBTReferenceFilter BLOCK_FILTER = NBTReferenceFilter.create(
			ref -> ref.getItem().getItem() instanceof BlockItem,
			ref -> true,
			null,
			TextInst.translatable("nbteditor.no_ref.block"),
			TextInst.translatable("nbteditor.no_hand.no_item.block"));
	
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
			NBTReference.getReference(BLOCK_FILTER, false, ref -> MainUtil.client.setScreen(new BlockStatesScreen<>(ref)));
			return Command.SINGLE_SUCCESS;
		});
	}
	
}
