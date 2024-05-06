package com.luneruniverse.minecraft.mod.nbteditor.commands.factories;

import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.argument;
import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.literal;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.commands.arguments.FancyTextArgumentType;
import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.screens.factories.DisplayScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.Lore;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.item.ItemStack;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class LoreCommand extends ClientCommand {
	
	@Override
	public String getName() {
		return "lore";
	}
	
	@Override
	public String getExtremeAlias() {
		return "l";
	}
	
	@Override
	public void register(LiteralArgumentBuilder<FabricClientCommandSource> builder, String path) {
		Command<FabricClientCommandSource> add = context -> {
			Text line = context.getArgument("text", Text.class);
			int pos = -1;
			try {
				pos = context.getArgument("line", Integer.class);
			} catch (IllegalArgumentException e) {}
			
			ItemReference ref = ItemReference.getHeldItem();
			ItemStack item = ref.getItem();
			
			Lore lore = new Lore(item);
			lore.addLine(line, pos);
			ref.saveItem(item, TextInst.translatable("nbteditor.lore.edited"));
			
			return Command.SINGLE_SUCCESS;
		};
		Command<FabricClientCommandSource> remove = context -> {
			int pos = -1;
			try {
				pos = context.getArgument("line", Integer.class);
			} catch (IllegalArgumentException e) {}
			
			ItemReference ref = ItemReference.getHeldItem();
			ItemStack item = ref.getItem();
			
			Lore lore = new Lore(item);
			lore.removeLine(pos);
			ref.saveItem(item, TextInst.translatable("nbteditor.lore.edited"));
			
			return Command.SINGLE_SUCCESS;
		};
		Command<FabricClientCommandSource> set = context -> {
			Text line = context.getArgument("text", Text.class);
			int pos = -1;
			try {
				pos = context.getArgument("line", Integer.class);
			} catch (IllegalArgumentException e) {}
			
			ItemReference ref = ItemReference.getHeldItem();
			ItemStack item = ref.getItem();
			
			Lore lore = new Lore(item);
			lore.setLine(line, pos);
			ref.saveItem(item, TextInst.translatable("nbteditor.lore.edited"));
			
			return Command.SINGLE_SUCCESS;
		};
		Command<FabricClientCommandSource> clear = context -> {
			ItemReference ref = ItemReference.getHeldItem();
			ItemStack item = ref.getItem();
			
			Lore lore = new Lore(item);
			lore.clearLore();
			ref.saveItem(item, TextInst.translatable("nbteditor.lore.edited"));
			
			return Command.SINGLE_SUCCESS;
		};
		Command<FabricClientCommandSource> list = context -> {
			ItemReference heldItem = ItemReference.getHeldItem(item -> true, TextInst.translatable("nbteditor.no_hand.no_item.to_view"));
			ItemStack item = heldItem.getItem();
			
			context.getSource().sendFeedback(TextInst.literal("[").formatted(Formatting.GRAY).append(TextInst.literal("+").formatted(Formatting.GREEN)).append(TextInst.literal("] ").formatted(Formatting.GRAY))
					.styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/factory display lore add "))
							.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextInst.of("/factory display lore add"))))
					.append(TextInst.literal("[").formatted(Formatting.GRAY).append(TextInst.literal("Clear").formatted(Formatting.RED)).append(TextInst.literal("] ").formatted(Formatting.GRAY))
					.styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/factory display lore clear"))
							.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextInst.of("/factory display lore clear"))))));
			
			Lore lore = new Lore(item);
			int i = 0;
			for (Text line : lore.getLore()) {
				final int finalI = i;
				context.getSource().sendFeedback(TextInst.literal("[").formatted(Formatting.GRAY).append(TextInst.literal("-").formatted(Formatting.RED)).append(TextInst.literal("]").formatted(Formatting.GRAY))
						.styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/factory display lore remove " + finalI))
								.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextInst.of("/factory display lore remove " + finalI))))
						.append(TextInst.literal(" ").formatted(Formatting.DARK_PURPLE).formatted(Formatting.ITALIC).append(line)
						.styled(style -> MixinLink.withRunClickEvent(style, () -> MainUtil.client.currentScreen.handleTextClick(Style.EMPTY.withClickEvent(
									new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/factory display lore set " + finalI + " " + FancyTextArgumentType.stringifyFancyText(line, true, true)))))
								.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextInst.of("/factory display lore set " + finalI))))));
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
				.then(literal("list").executes(list))
			.executes(context -> {
				MainUtil.client.setScreen(new DisplayScreen(ItemReference.getHeldItem()));
				return Command.SINGLE_SUCCESS;
			});
	}
	
}
