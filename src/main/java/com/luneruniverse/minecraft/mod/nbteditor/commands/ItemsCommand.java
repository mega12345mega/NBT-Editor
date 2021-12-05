package com.luneruniverse.minecraft.mod.nbteditor.commands;

import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.literal;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.luneruniverse.minecraft.mod.nbteditor.screens.ItemsHandler;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ItemsScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;

public class ItemsCommand implements RegisterableCommand {
	
	@Override
	public LiteralCommandNode<? extends CommandSource> register(boolean dedicated) {
		return ClientCommandManager.DISPATCHER.register(literal("items").executes(context -> {
			ClientPlayerEntity player = context.getSource().getPlayer();
			Map.Entry<Hand, ItemStack> heldItem = MainUtil.getHeldItem(player, this::isContainer, new TranslatableText("nbteditor.nocontainer"));
			Hand hand = heldItem.getKey();
			ItemStack item = heldItem.getValue();
			
			PlayerInventory inv = player.getInventory();
			ItemsHandler handler = new ItemsHandler(0, inv);
			MinecraftClient.getInstance().setScreen(new ItemsScreen(handler, inv, new TranslatableText("nbteditor.items").append(item.getName()))
					.build(item, hand));
			
			return Command.SINGLE_SUCCESS;
		}));
	}
	private boolean isContainer(ItemStack item) {
		return item.getItem() instanceof BlockItem && (
				((BlockItem) item.getItem()).getBlock() instanceof ShulkerBoxBlock ||
				((BlockItem) item.getItem()).getBlock() instanceof ChestBlock ||
				((BlockItem) item.getItem()).getBlock() instanceof BarrelBlock
			);
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
