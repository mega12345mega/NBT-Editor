package com.luneruniverse.minecraft.mod.nbteditor.commands;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import java.util.Arrays;
import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

public class NBTCommand implements ClientCommand {
	
	@Override
	public LiteralCommandNode<FabricClientCommandSource> register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess cmdReg) {
		return dispatcher.register(literal("nbteditor").then(literal("config").executes(context -> {
			MainUtil.client.setScreen(new ConfigScreen(null));
			return Command.SINGLE_SUCCESS;
		})).then(literal("new").then(argument("item", ItemStackArgumentType.itemStack(cmdReg)).executes(context -> {
			ItemStack newItem = context.getArgument("item", ItemStackArgument.class).createStack(1, true);
			Hand hand;
			if (MainUtil.client.player.getMainHandStack().isEmpty())
				hand = Hand.MAIN_HAND;
			else if (MainUtil.client.player.getOffHandStack().isEmpty())
				hand = Hand.OFF_HAND;
			else {
				MainUtil.client.player.sendMessage(Text.translatable("nbteditor.allitem"));
				return Command.SINGLE_SUCCESS;
			}
			MainUtil.saveItem(hand, newItem);
			MainUtil.client.setScreen(new NBTEditorScreen(new ItemReference(hand)));
			return Command.SINGLE_SUCCESS;
		}))).executes(context -> {
			MainUtil.client.setScreen(new NBTEditorScreen(ConfigScreen.isAirEditable() ? MainUtil.getHeldItemAirable() : MainUtil.getHeldItem()));
			return Command.SINGLE_SUCCESS;
		}));
	}
	
	@Override
	public List<String> getAliases() {
		return Arrays.asList("nbt");
	}
	
}
