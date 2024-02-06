package com.luneruniverse.minecraft.mod.nbteditor.commands.factories;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
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
			if (item.getOrCreateNbt().getBoolean("Unbreakable")) {
				item.getNbt().remove("Unbreakable");
				ref.saveItem(item, TextInst.translatable("nbteditor.unbreakable.disabled"));
			} else {
				item.getNbt().putBoolean("Unbreakable", true);
				ref.saveItem(item, TextInst.translatable("nbteditor.unbreakable.enabled"));
			}
			return Command.SINGLE_SUCCESS;
		});
	}
	
}
