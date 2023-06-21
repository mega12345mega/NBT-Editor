package com.luneruniverse.minecraft.mod.nbteditor.commands.arguments;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;
import java.util.stream.StreamSupport;

import com.google.gson.JsonElement;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.EditableText;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class FancyTextArgumentType implements ArgumentType<Text> {
	
	private static final Style BLANK_STYLE = Style.EMPTY.withColor(Formatting.WHITE)
			.withBold(false).withItalic(false).withUnderline(false).withStrikethrough(false).withObfuscated(false);
	private static final List<String> eventTypes = List.of("[open_url]", "[run_command]", "[suggest_command]",
			"[change_page]", "[copy_to_clipboard]", "[show_text]", "[show_item]", "[show_entity]");
	
	public static FancyTextArgumentType fancyText(boolean jsonAllowed) {
		return new FancyTextArgumentType(jsonAllowed);
	}
	public static FancyTextArgumentType fancyText() {
		return fancyText(true);
	}
	
	public static String stringifyFancyText(Text text, boolean jsonAllowed, boolean printErrors) {
		if (jsonAllowed && ConfigScreen.isJsonText())
			return Text.Serializer.toJson(text);
		
		StringBuilder output = new StringBuilder();
		if (stringifyFancyText(text, output) && printErrors)
			MainUtil.client.player.sendMessage(TextInst.translatable("nbteditor.fancy_text_arg_type.stringify_unsupported"), false);
		return output.toString();
	}
	private static boolean stringifyFancyText(Text text, StringBuilder output) {
		boolean errors = false;
		
		int numEvents = 0;
		ClickEvent click = text.getStyle().getClickEvent();
		HoverEvent hover = text.getStyle().getHoverEvent();
		if (click != null) {
			numEvents++;
			output.append("[" + click.getAction().getName() + "]");
			output.append("{");
			output.append(click.getValue().replace("\\", "\\\\").replace("{", "\\{").replace("}", "\\}"));
			output.append("}");
			output.append("(");
		}
		if (hover != null) {
			numEvents++;
			output.append("[" + hover.getAction().getName() + "]");
			if (hover.getAction() == HoverEvent.Action.SHOW_TEXT) {
				StringBuilder buffer = new StringBuilder();
				errors |= stringifyFancyText(hover.getValue(HoverEvent.Action.SHOW_TEXT), buffer);
				output.append("{" + buffer.toString().replace("}", "\\}") + "}");
			} else if (hover.getAction() == HoverEvent.Action.SHOW_ITEM)
				errors = true;
			else if (hover.getAction() == HoverEvent.Action.SHOW_ENTITY) {
				output.append("{" + hover.getValue(HoverEvent.Action.SHOW_ENTITY).uuid.toString() + "}");
				errors = true;
			}
			output.append("(");
		}
		
		StringBuilder color = new StringBuilder();
		StringBuilder formats = new StringBuilder();
		boolean needsReset = false;
		for (Map.Entry<String, JsonElement> entry : Text.Serializer.toJsonTree(text).getAsJsonObject().entrySet()) {
			switch (entry.getKey()) {
				case "color" -> {
					Formatting colorFormatting = Formatting.byName(text.getStyle().getColor().getName());
					if (colorFormatting == null) {
						color.append("&c");
						errors = true;
					} else
						color.append("&" + colorFormatting.getCode());
				}
				case "bold", "italic", "strikethrough", "obfuscated" -> {
					if (entry.getValue().getAsBoolean())
						formats.append("&" + Formatting.byName(entry.getKey()).getCode());
					else
						needsReset = true;
				}
				case "underlined" -> { // Formatting.UNDERLINE isn't past tense
					if (entry.getValue().getAsBoolean())
						formats.append("&" + Formatting.UNDERLINE.getCode());
					else
						needsReset = true;
				}
				case "insertion", "font" -> errors = true;
			}
		}
		if (needsReset)
			output.append("&r");
		if (!(needsReset && color.toString().equals("&f")))
			output.append(color);
		output.append(formats);
		
		StringBuilder content = new StringBuilder();
		text.getContent().visit(str -> {
			content.append(str);
			return Optional.empty();
		});
		output.append(content.toString().replace("\\", "\\\\").replace("&", "\\&").replace("§", "\\§").replace("[", "\\["));
		
		for (Text child : text.getSiblings())
			errors |= stringifyFancyText(child, output);
		
		output.append(")".repeat(numEvents));
		
		return errors;
	}
	
	private final boolean jsonAllowed;
	
	private FancyTextArgumentType(boolean jsonAllowed) {
		this.jsonAllowed = jsonAllowed;
	}
	
	@Override
	public Text parse(StringReader reader) throws CommandSyntaxException {
		if (jsonAllowed && ConfigScreen.isJsonText())
			return TextArgumentType.text().parse(reader);
		
		return parseInternal(reader);
	}
	
	private EditableText parseInternal(StringReader reader) throws CommandSyntaxException {
		String str = "";
		boolean event = false;
		
		boolean escaped = false;
		while (reader.canRead()) {
			char c = reader.read();
			if (escaped) {
				str += '\\'; // Escaped characters are handled by parseColors
				str += c;
				escaped = false;
			} else if (c == '\\')
				escaped = true;
			else if (c == '[') {
				event = true;
				break;
			} else
				str += c;
		}
		
		EditableText output = parseColors(str);
		if (event) {
			UnaryOperator<Style> eventAdder;
			String eventType = reader.readStringUntil(']');
			switch (eventType) {
				case "open_url", "run_command", "suggest_command", "change_page", "copy_to_clipboard" -> {
					reader.expect('{');
					String value = readUntilClosed(reader, '{', '}');
					eventAdder = style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.byName(eventType), value));
				}
				case "show_text" -> {
					reader.expect('{');
					EditableText tooltip = parseInternal(new StringReader(readUntilClosed(reader, '{', '}')));
					eventAdder = style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip));
				}
				case "show_item" -> {
					ItemStack item;
					if (reader.peek() == '{') {
						reader.skip();
						int slot = reader.readInt();
						reader.expect('}');
						item = MainUtil.client.player.getInventory().getStack(slot);
					} else
						item = MainUtil.getHeldItem().getItem();
					eventAdder = style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM,
							new HoverEvent.ItemStackContent(item)));
				}
				case "show_entity" -> {
					Entity entity;
					if (reader.peek() == '{') {
						reader.skip();
						String uuid = reader.readStringUntil('}');
						if (!uuid.contains("-"))
							uuid = uuid.replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5");
						UUID uuidObj = UUID.fromString(uuid);
						entity = StreamSupport.stream(MainUtil.client.world.getEntities().spliterator(), false)
								.filter(testEntity -> testEntity.getUuid().equals(uuidObj)).findFirst().orElseThrow(
								() -> new SimpleCommandExceptionType(TextInst.translatable("nbteditor.fancy_text_arg_type.invalid.entity")).createWithContext(reader));
					} else if (MainUtil.client.targetedEntity != null)
						entity = MainUtil.client.targetedEntity;
					else
						entity = MainUtil.client.player;
					eventAdder = style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ENTITY,
							new HoverEvent.EntityContent(entity.getType(), entity.getUuid(), entity.getName())));
				}
				default -> throw new SimpleCommandExceptionType(TextInst.translatable("nbteditor.fancy_text_arg_type.invalid.event_type")).createWithContext(reader);
			}
			reader.expect('(');
			
			EditableText affectedText = parseInternal(new StringReader(readUntilClosed(reader, '(', ')')));
			affectedText.styled(eventAdder);
			output.append(affectedText);
		}
		if (reader.canRead())
			output.append(parseInternal(reader));
		
		return output;
	}
	private EditableText parseColors(String str) {
		List<EditableText> parts = new ArrayList<>();
		String part = "";
		Formatting color = null;
		List<Formatting> format = new ArrayList<>();
		boolean reset = false;
		
		boolean escaped = false;
		boolean formatting = false;
		for (char c : str.toCharArray()) {
			if (formatting) {
				formatting = false;
				Formatting newFormat = Formatting.byCode(c);
				if (newFormat == null)
					part += c;
				else {
					if (!part.isEmpty()) {
						EditableText partText = TextInst.literal(part);
						if (reset) {
							partText.styled(style -> BLANK_STYLE);
							if (parts.isEmpty()) // The first part has its formatting applied to children
								reset = false; // So no need to reset in children too
						}
						if (color != null) partText.formatted(color);
						format.forEach(partText::formatted);
						parts.add(partText);
						part = "";
					}
					if (newFormat == Formatting.RESET) {
						color = null;
						format.clear();
						reset = true;
					} else if (newFormat.isColor()) {
						color = newFormat;
						format.clear();
					} else
						format.add(newFormat);
				}
			} else if (escaped) {
				part += c;
				escaped = false;
			} else if (c == '\\')
				escaped = true;
			else if (c == '&' || c == '§')
				formatting = true;
			else
				part += c;
		}
		if (!part.isEmpty()) {
			EditableText partText = TextInst.literal(part);
			if (reset) partText.styled(style -> BLANK_STYLE);
			if (color != null) partText.formatted(color);
			format.forEach(partText::formatted);
			parts.add(partText);
		}
		
		if (parts.isEmpty())
			return TextInst.literal("");
		
		EditableText output = parts.get(0);
		for (int i = 1; i < parts.size(); i++)
			output.append(parts.get(i));
		return output;
	}
	private String readUntilClosed(StringReader reader, char open, char close) throws CommandSyntaxException {
		StringBuilder output = new StringBuilder();
		int level = 0;
		boolean escaped = false;
		while (reader.canRead()) {
			char c = reader.read();
			if (escaped)
				escaped = false;
			else if (c == '\\')
				escaped = true; // Include backslash in output
			else if (c == open)
				level++;
			else if (c == close) {
				level--;
				if (level == -1)
					break;
			}
			output.append(c);
		}
		if (level != -1)
			reader.expect(close); // throws
		return output.toString();
	}
	
	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		if (jsonAllowed && ConfigScreen.isJsonText())
			return TextArgumentType.text().listSuggestions(context, builder);
		
		if (builder.getRemaining().isEmpty())
			return builder.buildFuture();
		
		int lastColor = Math.max(builder.getRemaining().lastIndexOf('&'), builder.getRemaining().lastIndexOf('§'));
		int lastEvent = builder.getRemaining().lastIndexOf('[');
		int lastCloseEvent = builder.getRemaining().lastIndexOf(']');
		int lastValue = builder.getRemaining().lastIndexOf('{');
		int lastCloseValue = builder.getRemaining().lastIndexOf('}');
		int lastIndex = builder.getRemaining().length() - 1;
		
		if (lastColor == lastIndex) {
			builder = builder.createOffset(builder.getStart() + lastColor + 1);
			for (Formatting format : Formatting.values())
				builder.suggest(format.getCode() + "", () -> format.getName());
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
