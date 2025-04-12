package com.luneruniverse.minecraft.mod.nbteditor.commands.arguments;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import com.luneruniverse.minecraft.mod.nbteditor.fancytext.FancyText;
import com.luneruniverse.minecraft.mod.nbteditor.fancytext.StyleOption;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.luneruniverse.minecraft.mod.nbteditor.util.StyleUtil;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class FancyTextArgumentType implements ArgumentType<Text> {
	
	private static final List<String> eventTypes = Stream.of(StyleOption.values())
			.map(action -> "[" + action.name().toLowerCase() + "]").toList();
	
	public static FancyTextArgumentType fancyText(Style base) {
		return new FancyTextArgumentType(base);
	}
	
	public interface UnparsedText {
		public Text parse(Style base) throws CommandSyntaxException;
	}
	public static ArgumentType<UnparsedText> fancyText() {
		FancyTextArgumentType defaultArgType = new FancyTextArgumentType(Style.EMPTY);
		
		return new ArgumentType<>() {
			@Override
			public UnparsedText parse(StringReader reader) throws CommandSyntaxException {
				return style -> new FancyTextArgumentType(style).parse(reader);
			}
			@Override
			public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
				return defaultArgType.listSuggestions(context, builder);
			}
		};
	}
	
	public static String stringifyFancyText(Text text, Style base, boolean printErrors) {
		if (ConfigScreen.isJsonText())
			return TextInst.toJsonString(text);
		
		Map.Entry<String, Boolean> output = FancyText.stringify(text, base);
		if (output.getValue() && printErrors)
			MainUtil.client.player.sendMessage(TextInst.translatable("nbteditor.fancy_text_arg_type.stringify_unsupported"), false);
		return output.getKey();
	}
	
	private final Style base;
	
	private FancyTextArgumentType(Style base) {
		this.base = base;
	}
	
	@Override
	public Text parse(StringReader reader) throws CommandSyntaxException {
		if (ConfigScreen.isJsonText())
			return MVMisc.getTextArg().parse(reader);
		
		Text output = FancyText.parse(reader.getRemaining(), base);
		reader.setCursor(reader.getTotalLength());
		return output;
	}
	
	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		if (ConfigScreen.isJsonText())
			return MVMisc.getTextArg().listSuggestions(context, builder);
		
		if (builder.getRemaining().isEmpty())
			return builder.buildFuture();
		
		int lastColor = Math.max(builder.getRemaining().lastIndexOf('&'), builder.getRemaining().lastIndexOf('§'));
		int lastShadowColor = builder.getRemaining().lastIndexOf('_');
		int lastEvent = builder.getRemaining().lastIndexOf('[');
		int lastCloseEvent = builder.getRemaining().lastIndexOf(']');
		int lastValue = builder.getRemaining().lastIndexOf('{');
		int lastCloseValue = builder.getRemaining().lastIndexOf('}');
		int lastIndex = builder.getRemaining().length() - 1;
		
		if (lastColor == lastIndex || StyleUtil.SHADOW_COLOR_EXISTS && lastColor == lastShadowColor - 1 && lastShadowColor == lastIndex) {
			builder = builder.createOffset(builder.getStart() + lastIndex + 1);
			for (Formatting format : Formatting.values())
				builder.suggest(format.getCode() + "", () -> format.getName());
			builder.suggest("#", TextInst.translatable("nbteditor.fancy_text_arg_type.custom_color"));
			if (StyleUtil.SHADOW_COLOR_EXISTS && lastColor == lastIndex)
				builder.suggest("_", TextInst.translatable("nbteditor.fancy_text_arg_type.shadow_color"));
		} else if (lastCloseValue == lastIndex) {
			if (lastValue != -1) {
				builder = builder.createOffset(builder.getStart() + lastCloseValue + 1);
				builder.suggest("(");
			}
		} else if (lastCloseEvent == lastIndex) {
			if (lastEvent != -1) {
				String event = builder.getRemaining().substring(lastEvent, lastCloseEvent + 1);
				if (eventTypes.contains(event)) {
					builder = builder.createOffset(builder.getStart() + lastCloseEvent + 1);
					builder.suggest("{");
					if (event.equals("[show_item]") || event.equals("[show_entity]"))
						builder.suggest("(");
				}
			}
		} else if (lastEvent > lastCloseEvent) {
			builder = builder.createOffset(builder.getStart() + lastEvent);
			String event = builder.getRemaining();
			for (String suggestEvent : eventTypes) {
				if (suggestEvent.startsWith(event))
					builder.suggest(suggestEvent);
			}
		}
		
		return builder.buildFuture();
	}
	
}
