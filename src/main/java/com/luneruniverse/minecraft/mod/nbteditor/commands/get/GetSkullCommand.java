package com.luneruniverse.minecraft.mod.nbteditor.commands.get;

import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.argument;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;

public class GetSkullCommand extends ClientCommand {
	
	@Override
	public String getName() {
		return "skull";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
		builder.then(argument("player", StringArgumentType.word()).executes(context -> {
			ItemStack item = new ItemStack(Items.PLAYER_HEAD, 1);
			NbtCompound nbt = item.getOrCreateNbt();
			nbt.putString("SkullOwner", context.getArgument("player", String.class));
			MainUtil.getWithMessage(item);
			return Command.SINGLE_SUCCESS;
		}));
	}
	
}
