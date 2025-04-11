package com.luneruniverse.minecraft.mod.nbteditor.commands.factories;

import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.argument;
import static com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandManager.literal;

import java.util.ArrayList;
import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.commands.ClientCommand;
import com.luneruniverse.minecraft.mod.nbteditor.commands.arguments.FancyTextArgumentType;
import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.FabricClientCommandSource;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.screens.factories.DisplayScreen;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.ItemTagReferences;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.luneruniverse.minecraft.mod.nbteditor.util.TextUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.item.ItemStack;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class LoreCommand extends ClientCommand {
	
	private static int getPos(int pos, List<Text> lore, boolean afterLast) throws CommandSyntaxException {
		if (pos < 0)
			pos = pos + lore.size() + (afterLast ? 1 : 0);
		if (pos < 0 || pos > lore.size() || (!afterLast && pos == lore.size()))
			throw new SimpleCommandExceptionType(TextInst.translatable("nbteditor.lore.invalid_line")).create();
		return pos;
	}
	
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
			
			List<Text> lore = ItemTagReferences.LORE.get(item);
			lore.add(getPos(pos, lore, true), line);
			ItemTagReferences.LORE.set(item, lore);
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
			
			List<Text> lore = ItemTagReferences.LORE.get(item);
			lore.remove(getPos(pos, lore, false));
			ItemTagReferences.LORE.set(item, lore);
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
			
			List<Text> lore = ItemTagReferences.LORE.get(item);
			lore.set(getPos(pos, lore, false), line);
			ItemTagReferences.LORE.set(item, lore);
			ref.saveItem(item, TextInst.translatable("nbteditor.lore.edited"));
			
			return Command.SINGLE_SUCCESS;
		};
		Command<FabricClientCommandSource> clear = context -> {
			ItemReference ref = ItemReference.getHeldItem();
			ItemStack item = ref.getItem();
			
			ItemTagReferences.LORE.set(item, new ArrayList<>());
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
			
			List<Text> lore = ItemTagReferences.LORE.get(item);
			int i = 0;
			for (Text line : lore) {
				final int finalI = i;
				context.getSource().sendFeedback(TextInst.literal("[").formatted(Formatting.GRAY).append(TextInst.literal("-").formatted(Formatting.RED)).append(TextInst.literal("]").formatted(Formatting.GRAY))
						.styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/factory display lore remove " + finalI))
								.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextInst.of("/factory display lore remove " + finalI))))
						.append(TextInst.literal(" ").formatted(Formatting.DARK_PURPLE).formatted(Formatting.ITALIC).append(line)
						.styled(style -> MixinLink.withRunClickEvent(style, () -> MainUtil.client.currentScreen.handleTextClick(Style.EMPTY.withClickEvent(
									new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/factory display lore set " + finalI + " " + FancyTextArgumentType.stringifyFancyText(line, TextUtil.BASE_LORE_STYLE, true)))))
								.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextInst.of("/factory display lore set " + finalI))))));
				i++;
			}
			if (lore.isEmpty())
				context.getSource().sendFeedback(TextInst.translatable("nbteditor.lore.none"));
			
			return Command.SINGLE_SUCCESS;
		};
		
		builder.then(literal("add")
						.then(argument("line", IntegerArgumentType.integer()).then(argument("text", FancyTextArgumentType.fancyText(TextUtil.BASE_LORE_STYLE)).executes(add)))
						.then(argument("text", FancyTextArgumentType.fancyText(TextUtil.BASE_LORE_STYLE)).executes(add)))
				.then(literal("remove")
						.then(argument("line", IntegerArgumentType.integer()).executes(remove))
						.executes(remove))
				.then(literal("set")
						.then(argument("line", IntegerArgumentType.integer()).then(argument("text", FancyTextArgumentType.fancyText(TextUtil.BASE_LORE_STYLE)).executes(set)))
						.then(argument("text", FancyTextArgumentType.fancyText(TextUtil.BASE_LORE_STYLE)).executes(set)))
				.then(literal("clear").executes(clear))
				.then(literal("list").executes(list))
			.executes(context -> {
				MainUtil.client.setScreen(new DisplayScreen<>(ItemReference.getHeldItem()));
				return Command.SINGLE_SUCCESS;
			});
	}
	
}
