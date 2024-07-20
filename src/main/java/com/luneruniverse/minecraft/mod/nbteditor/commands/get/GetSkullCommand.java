package com.luneruniverse.minecraft.mod.nbteditor.commands.get;

import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.argument;

import java.util.Optional;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.NBTManagers;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.ItemTagReferences;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.StringHelper;

public class GetSkullCommand extends ClientCommand {
	
	@Override
	public String getName() {
		return "skull";
	}
	
	@Override
	public String getExtremeAlias() {
		return "sk";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder, String path) {
		builder.then(argument("player", StringArgumentType.word()).executes(context -> {
			String player = context.getArgument("player", String.class);
			if (NBTManagers.COMPONENTS_EXIST && !StringHelper.isValidPlayerName(player)) {
				MainUtil.client.player.sendMessage(TextInst.translatable("nbteditor.skull.invalid_player_name"), false);
				return Command.SINGLE_SUCCESS;
			}
			ItemStack item = new ItemStack(Items.PLAYER_HEAD, 1);
			ItemTagReferences.PROFILE_NAME.set(item, Optional.of(player));
			MainUtil.getWithMessage(item);
			return Command.SINGLE_SUCCESS;
		}));
	}
	
}
