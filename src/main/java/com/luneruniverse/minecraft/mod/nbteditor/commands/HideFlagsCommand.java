package com.luneruniverse.minecraft.mod.nbteditor.commands;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.luneruniverse.minecraft.mod.nbteditor.screens.HideFlagsScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class HideFlagsCommand implements ClientCommand {
	
	@Override
	public LiteralCommandNode<FabricClientCommandSource> register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess cmdReg) {
		return dispatcher.register(literal("hideflags").executes(context -> {
			ClientPlayerEntity player = context.getSource().getPlayer();
			Map.Entry<Hand, ItemStack> heldItem = MainUtil.getHeldItem(player);
			Hand hand = heldItem.getKey();
			ItemStack item = heldItem.getValue();
			
			MainUtil.client.setScreen(new HideFlagsScreen(item, hand));
			
			return Command.SINGLE_SUCCESS;
		}));
	}
	
	@Override
	public List<String> getAliases() {
		return Arrays.asList("removeflags");
	}
	
}
