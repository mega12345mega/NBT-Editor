package com.luneruniverse.minecraft.mod.nbteditor.commands;

import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.literal;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.luneruniverse.minecraft.mod.nbteditor.screens.ItemsScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;

public class ItemsCommand implements RegisterableCommand {
	
	@Override
	public LiteralCommandNode<? extends CommandSource> register(boolean dedicated) {
		return ClientCommandManager.DISPATCHER.register(literal("items").executes(context -> {
			ClientPlayerEntity player = context.getSource().getPlayer();
			Map.Entry<Hand, ItemStack> heldItem = MainUtil.getHeldItem(player, ItemsScreen::isContainer, new TranslatableText("nbteditor.nocontainer"));
			Hand hand = heldItem.getKey();
			
			ItemsScreen.show(new ItemReference(hand));
			
			return Command.SINGLE_SUCCESS;
		}));
	}
	
	@Override
	public List<String> getAliases() {
		return Arrays.asList("open");
	}
	
	@Override
	public EnvType getSide() {
		return EnvType.CLIENT;
	}
	
}
