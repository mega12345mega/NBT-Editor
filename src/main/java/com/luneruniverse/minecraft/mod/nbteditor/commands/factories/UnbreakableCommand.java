package com.luneruniverse.minecraft.mod.nbteditor.commands.factories;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.ItemTagReferences;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.item.ItemStack;

public class UnbreakableCommand extends ClientCommand {
	
	@Override
	public String getName() {
		return "unbreakable";
	}
	
	@Override
	public String getExtremeAlias() {
		return "ub";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder, String path) {
		builder.executes(context -> {
			ItemReference ref = ItemReference.getHeldItem();
			ItemStack item = ref.getItem();
			boolean unbreakable = !ItemTagReferences.UNBREAKABLE.get(item);
			ItemTagReferences.UNBREAKABLE.set(item, unbreakable);
			ref.saveItem(item, TextInst.translatable("nbteditor.unbreakable." + (unbreakable ? "enabled" : "disabled")));
			return Command.SINGLE_SUCCESS;
		});
	}
	
}
