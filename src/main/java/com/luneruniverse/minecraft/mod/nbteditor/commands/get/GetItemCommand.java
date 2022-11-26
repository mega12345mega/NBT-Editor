package com.luneruniverse.minecraft.mod.nbteditor.commands.get;

import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.argument;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.item.ItemStack;

public class GetItemCommand extends ClientCommand {
	
	@Override
	public String getName() {
		return "item";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
		Command<FabricClientCommandSource> getItem = context -> {
			int count = getDefaultArg(context, "count", 1, Integer.class);
			ItemStack item = context.getArgument("item", ItemStackArgument.class).createStack(count, false);
			MainUtil.getWithMessage(item);
			return Command.SINGLE_SUCCESS;
		};
		
		builder.then(argument("item", MultiVersionMisc.getItemStackArg())
				.then(argument("count", IntegerArgumentType.integer(1)).executes(getItem)).executes(getItem));
	}
	
}
