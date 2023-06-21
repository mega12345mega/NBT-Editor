package com.luneruniverse.minecraft.mod.nbteditor.commands.nbt;

import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.argument;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.argument.ItemStackArgument;

public class NBTNewCommand extends ClientCommand {
	
	@Override
	public String getName() {
		return "new";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
		builder.then(argument("item", MultiVersionMisc.getItemStackArg()).executes(context -> {
			ItemReference ref = MainUtil.getHeldAir();
			ref.saveItem(context.getArgument("item", ItemStackArgument.class).createStack(1, true));
			MainUtil.client.setScreen(new NBTEditorScreen(ref));
			return Command.SINGLE_SUCCESS;
		}));
	}
	
}
