package com.luneruniverse.minecraft.mod.nbteditor.commands;

import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.literal;

import java.util.Map;

import com.luneruniverse.minecraft.mod.nbteditor.util.Lore;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;

public class LoreCommand implements RegisterableCommand {
	
	@Override
	public LiteralCommandNode<FabricClientCommandSource> register(boolean dedicated) {
		Command<FabricClientCommandSource> add = context -> {
			Text line = context.getArgument("text", Text.class);
			int pos = -1;
			try {
				pos = context.getArgument("line", Integer.class);
			} catch (IllegalArgumentException e) {}
			
			ClientPlayerEntity player = context.getSource().getPlayer();
			Map.Entry<Hand, ItemStack> heldItem = MainUtil.getHeldItem(player);
			Hand hand = heldItem.getKey();
			ItemStack item = heldItem.getValue();
			
			Lore lore = new Lore(item);
			lore.addLine(line, pos);
			MainUtil.saveItem(hand, item);
			
			context.getSource().sendFeedback(new TranslatableText("nbteditor.lore_edited"));
			
			return Command.SINGLE_SUCCESS;
		};
		Command<FabricClientCommandSource> remove = context -> {
			int pos = -1;
			try {
				pos = context.getArgument("line", Integer.class);
			} catch (IllegalArgumentException e) {}
			
			ClientPlayerEntity player = context.getSource().getPlayer();
			Map.Entry<Hand, ItemStack> heldItem = MainUtil.getHeldItem(player);
			Hand hand = heldItem.getKey();
			ItemStack item = heldItem.getValue();
			
			Lore lore = new Lore(item);
			lore.removeLine(pos);
			MainUtil.saveItem(hand, item);
			
			context.getSource().sendFeedback(new TranslatableText("nbteditor.lore_edited"));
			
			return Command.SINGLE_SUCCESS;
		};
		Command<FabricClientCommandSource> set = context -> {
			Text line = context.getArgument("text", Text.class);
			int pos = -1;
			try {
				pos = context.getArgument("line", Integer.class);
			} catch (IllegalArgumentException e) {}
			
			ClientPlayerEntity player = context.getSource().getPlayer();
			Map.Entry<Hand, ItemStack> heldItem = MainUtil.getHeldItem(player);
			Hand hand = heldItem.getKey();
			ItemStack item = heldItem.getValue();
			
			Lore lore = new Lore(item);
			lore.setLine(line, pos);
			MainUtil.saveItem(hand, item);
			
			context.getSource().sendFeedback(new TranslatableText("nbteditor.lore_edited"));
			
			return Command.SINGLE_SUCCESS;
		};
		Command<FabricClientCommandSource> clear = context -> {
			ClientPlayerEntity player = context.getSource().getPlayer();
			Map.Entry<Hand, ItemStack> heldItem = MainUtil.getHeldItem(player);
			Hand hand = heldItem.getKey();
			ItemStack item = heldItem.getValue();
			
			Lore lore = new Lore(item);
			lore.clearLore();
			MainUtil.saveItem(hand, item);
			
			context.getSource().sendFeedback(new TranslatableText("nbteditor.lore_edited"));
			
			return Command.SINGLE_SUCCESS;
		};
		Command<FabricClientCommandSource> list = context -> {
			ClientPlayerEntity player = context.getSource().getPlayer();
			Map.Entry<Hand, ItemStack> heldItem = MainUtil.getHeldItem(player, item -> true, new TranslatableText("nbteditor.noitem_view"));
			ItemStack item = heldItem.getValue();
			
			context.getSource().sendFeedback(new LiteralText("[").formatted(Formatting.GRAY).append(new LiteralText("+").formatted(Formatting.GREEN)).append(new LiteralText("] ").formatted(Formatting.GRAY))
					.styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/lore add "))
							.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("/lore add"))))
					.append(new LiteralText("[").formatted(Formatting.GRAY).append(new LiteralText("Clear").formatted(Formatting.RED)).append(new LiteralText("] ").formatted(Formatting.GRAY))
					.styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/lore clear"))
							.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("/lore clear"))))));
			
			Lore lore = new Lore(item);
			int i = 0;
			for (Text line : lore.getLore()) {
				final int finalI = i;
				context.getSource().sendFeedback(new LiteralText("[").formatted(Formatting.GRAY).append(new LiteralText("-").formatted(Formatting.RED)).append(new LiteralText("]").formatted(Formatting.GRAY))
						.styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/lore remove " + finalI))
								.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("/lore remove " + finalI))))
						.append(new LiteralText(" ").formatted(Formatting.DARK_PURPLE).formatted(Formatting.ITALIC).append(line)
						.styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/lore set " + finalI + " " + Text.Serializer.toJson(line)))
								.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("/lore set " + finalI))))));
				i++;
			}
			if (lore.isEmpty())
				context.getSource().sendFeedback(new TranslatableText("nbteditor.nolore"));
			
			return Command.SINGLE_SUCCESS;
		};
		
		return ClientCommandManager.DISPATCHER.register(literal("lore")
				.then(literal("add")
						.then(argument("line", IntegerArgumentType.integer()).then(argument("text", TextArgumentType.text()).executes(add)))
						.then(argument("text", TextArgumentType.text()).executes(add)))
				.then(literal("remove")
						.then(argument("line", IntegerArgumentType.integer()).executes(remove))
						.executes(remove))
				.then(literal("set")
						.then(argument("line", IntegerArgumentType.integer()).then(argument("text", TextArgumentType.text()).executes(set)))
						.then(argument("text", TextArgumentType.text()).executes(set)))
				.then(literal("clear").executes(clear))
				.then(literal("list").executes(list))
			);
	}
	
	@Override
	public EnvType getSide() {
		return EnvType.CLIENT;
	}
	
}
