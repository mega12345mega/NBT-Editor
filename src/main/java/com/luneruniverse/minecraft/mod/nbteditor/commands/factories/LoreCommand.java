package com.luneruniverse.minecraft.mod.nbteditor.commands.factories;

import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.argument;
import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.literal;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.commands.arguments.FancyTextArgumentType;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.util.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.util.Lore;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.item.ItemStack;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;

public class LoreCommand extends ClientCommand {
	
	@Override
	public String getName() {
		return "lore";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
		Command<FabricClientCommandSource> add = context -> {
			Text line = context.getArgument("text", Text.class);
			int pos = -1;
			try {
				pos = context.getArgument("line", Integer.class);
			} catch (IllegalArgumentException e) {}
			
			ItemReference heldItem = MainUtil.getHeldItem();
			Hand hand = heldItem.getHand();
			ItemStack item = heldItem.getItem();
			
			Lore lore = new Lore(item);
			lore.addLine(line, pos);
			MainUtil.saveItem(hand, item);
			
			context.getSource().sendFeedback(TextInst.translatable("nbteditor.lore.edited"));
			
			return Command.SINGLE_SUCCESS;
		};
		Command<FabricClientCommandSource> remove = context -> {
			int pos = -1;
			try {
				pos = context.getArgument("line", Integer.class);
			} catch (IllegalArgumentException e) {}
			
			ItemReference heldItem = MainUtil.getHeldItem();
			Hand hand = heldItem.getHand();
			ItemStack item = heldItem.getItem();
			
			Lore lore = new Lore(item);
			lore.removeLine(pos);
			MainUtil.saveItem(hand, item);
			
			context.getSource().sendFeedback(TextInst.translatable("nbteditor.lore.edited"));
			
			return Command.SINGLE_SUCCESS;
		};
		Command<FabricClientCommandSource> set = context -> {
			Text line = context.getArgument("text", Text.class);
			int pos = -1;
			try {
				pos = context.getArgument("line", Integer.class);
			} catch (IllegalArgumentException e) {}
			
			ItemReference heldItem = MainUtil.getHeldItem();
			Hand hand = heldItem.getHand();
			ItemStack item = heldItem.getItem();
			
			Lore lore = new Lore(item);
			lore.setLine(line, pos);
			MainUtil.saveItem(hand, item);
			
			context.getSource().sendFeedback(TextInst.translatable("nbteditor.lore.edited"));
			
			return Command.SINGLE_SUCCESS;
		};
		Command<FabricClientCommandSource> clear = context -> {
			ItemReference heldItem = MainUtil.getHeldItem();
			Hand hand = heldItem.getHand();
			ItemStack item = heldItem.getItem();
			
			Lore lore = new Lore(item);
			lore.clearLore();
			MainUtil.saveItem(hand, item);
			
			context.getSource().sendFeedback(TextInst.translatable("nbteditor.lore.edited"));
			
			return Command.SINGLE_SUCCESS;
		};
		Command<FabricClientCommandSource> list = context -> {
			ItemReference heldItem = MainUtil.getHeldItem(item -> true, TextInst.translatable("nbteditor.no_hand.no_item.to_view"));
			ItemStack item = heldItem.getItem();
			
			context.getSource().sendFeedback(TextInst.literal("[").formatted(Formatting.GRAY).append(TextInst.literal("+").formatted(Formatting.GREEN)).append(TextInst.literal("] ").formatted(Formatting.GRAY))
					.styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/itemfactory lore add "))
							.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("/itemfactory lore add"))))
					.append(TextInst.literal("[").formatted(Formatting.GRAY).append(TextInst.literal("Clear").formatted(Formatting.RED)).append(TextInst.literal("] ").formatted(Formatting.GRAY))
					.styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/itemfactory lore clear"))
							.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("/itemfactory lore clear"))))));
			
			Lore lore = new Lore(item);
			int i = 0;
			for (Text line : lore.getLore()) {
				final int finalI = i;
				context.getSource().sendFeedback(TextInst.literal("[").formatted(Formatting.GRAY).append(TextInst.literal("-").formatted(Formatting.RED)).append(TextInst.literal("]").formatted(Formatting.GRAY))
						.styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/itemfactory lore remove " + finalI))
								.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("/itemfactory lore remove " + finalI))))
						.append(TextInst.literal(" ").formatted(Formatting.DARK_PURPLE).formatted(Formatting.ITALIC).append(line)
						.styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/itemfactory lore set " + finalI + " " + Text.Serializer.toJson(line)))
								.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("/itemfactory lore set " + finalI))))));
				i++;
			}
			if (lore.isEmpty())
				context.getSource().sendFeedback(TextInst.translatable("nbteditor.lore.none"));
			
			return Command.SINGLE_SUCCESS;
		};
		
		builder.then(literal("add")
						.then(argument("line", IntegerArgumentType.integer()).then(argument("text", FancyTextArgumentType.fancyText()).executes(add)))
						.then(argument("text", FancyTextArgumentType.fancyText()).executes(add)))
				.then(literal("remove")
						.then(argument("line", IntegerArgumentType.integer()).executes(remove))
						.executes(remove))
				.then(literal("set")
						.then(argument("line", IntegerArgumentType.integer()).then(argument("text", FancyTextArgumentType.fancyText()).executes(set)))
						.then(argument("text", FancyTextArgumentType.fancyText()).executes(set)))
				.then(literal("clear").executes(clear))
				.then(literal("list").executes(list));
	}
	
}
