package com.luneruniverse.minecraft.mod.nbteditor.snbtformatters;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.EditableText;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Formatting;

public class NormalSNBTFormatter1 extends SNBTFormatter {
	
	private static final Pattern INT_PATTERN = Pattern.compile(
			"(?<number>[-+]?(?:0|[1-9][0-9]*))(?<suffix>[bsl])?", Pattern.CASE_INSENSITIVE);
	private static final Pattern IMPLICIT_FLOAT_PATTERN = Pattern.compile(
			"(?<number>[-+]?(?:[0-9]+\\.|[0-9]*\\.[0-9]+)(?:e[-+]?[0-9]+)?)", Pattern.CASE_INSENSITIVE);
	private static final Pattern EXPLICIT_FLOAT_PATTERN = Pattern.compile(
			"(?<number>[-+]?(?:[0-9]+\\.?|[0-9]*\\.[0-9]+)(?:e[-+]?[0-9]+)?)(?<suffix>[fd])", Pattern.CASE_INSENSITIVE);
	
	@Override
	public Result format(String snbt, boolean allowSpecialNumbers) {
		StringReader reader = new StringReader(snbt);
		
		NbtElement nbt;
		try {
			nbt = MixinLink.parseSnbt(reader, allowSpecialNumbers, false);
		} catch (CommandSyntaxException e) {
			return Result.ofFailure(snbt);
		}
		
		reader.setCursor(0);
		EditableText text = TextInst.literal("");
		formatWhitespace(reader, text);
		formatElement(reader, text, allowSpecialNumbers);
		formatWhitespace(reader, text);
		
		return Result.ofSuccess(text, nbt);
	}
	
	private void formatElement(StringReader reader, EditableText text, boolean allowSpecialNumbers) {
		switch (reader.peek()) {
			case '{' -> formatCompound(reader, text, allowSpecialNumbers);
			case '[' -> formatList(reader, text, allowSpecialNumbers);
			default -> formatPrimitive(reader, text, allowSpecialNumbers);
		}
	}
	
	private void formatCompound(StringReader reader, EditableText text, boolean allowSpecialNumbers) {
		formatChar(reader, text); // '{'
		formatWhitespace(reader, text);
		
		while (reader.peek() != '}') {
			formatString(reader, text, NAME_COLOR);
			formatWhitespace(reader, text);
			formatChar(reader, text); // ':'
			formatWhitespace(reader, text);
			formatElement(reader, text, allowSpecialNumbers);
			formatWhitespace(reader, text);
			if (reader.peek() == ',') {
				formatChar(reader, text); // ','
				formatWhitespace(reader, text);
			} else {
				break;
			}
		}
		
		formatChar(reader, text); // '}'
	}
	
	private void formatList(StringReader reader, EditableText text, boolean allowSpecialNumbers) {
		formatChar(reader, text); // '['
		
		if (reader.canRead(2) && !StringReader.isQuotedStringStart(reader.peek()) && reader.peek(1) == ';') {
			text.append(TextInst.literal(String.valueOf(reader.read())).formatted(TYPE_SUFFIX_COLOR)); // B/I/L
			formatChar(reader, text); // ';'
		}
		
		formatWhitespace(reader, text);
		
		while (reader.peek() != ']') {
			formatElement(reader, text, allowSpecialNumbers);
			formatWhitespace(reader, text);
			if (reader.peek() == ',') {
				formatChar(reader, text); // ','
				formatWhitespace(reader, text);
			} else {
				break;
			}
		}
		
		formatChar(reader, text); // ']'
	}
	
	private void formatPrimitive(StringReader reader, EditableText text, boolean allowSpecialNumbers) {
		if (StringReader.isQuotedStringStart(reader.peek())) {
			formatString(reader, text, STRING_COLOR);
			return;
		}
		
		String snbt = reader.readUnquotedString();
		
		Matcher numberMatcher = MainUtil.matchAny(snbt, INT_PATTERN, IMPLICIT_FLOAT_PATTERN, EXPLICIT_FLOAT_PATTERN);
		if (numberMatcher != null) {
			String number = numberMatcher.group("number");
			String suffix = (numberMatcher.pattern() == IMPLICIT_FLOAT_PATTERN ? null : numberMatcher.group("suffix"));
			
			try {
				if (suffix == null) {
					if (numberMatcher.pattern() == INT_PATTERN)
						Integer.parseInt(number);
					else if (numberMatcher.pattern() == IMPLICIT_FLOAT_PATTERN)
						Double.parseDouble(number);
					else
						throw new IllegalArgumentException("Unknown pattern: " + numberMatcher.pattern().pattern()); // Impossible
				} else {
					switch (suffix) {
						case "b" -> Byte.parseByte(number);
						case "s" -> Short.parseShort(number);
						case "l" -> Long.parseLong(number);
						case "f" -> Float.parseFloat(number);
						case "d" -> Double.parseDouble(number);
						default -> throw new IllegalArgumentException("Unknown suffix: " + suffix); // Impossible
					}
				}
			} catch (NumberFormatException e) {
				text.append(TextInst.literal(snbt).formatted(STRING_COLOR));
				return;
			}
			
			text.append(TextInst.literal(number).formatted(NUMBER_COLOR));
			if (suffix != null)
				text.append(TextInst.literal(suffix).formatted(TYPE_SUFFIX_COLOR));
			
			return;
		}
		
		if (snbt.equalsIgnoreCase("true") || snbt.equalsIgnoreCase("false")) {
			text.append(TextInst.literal(snbt).formatted(NUMBER_COLOR));
			return;
		}
		
		if (allowSpecialNumbers && SPECIAL_NUMS.containsKey(snbt)) {
			text.append(TextInst.literal(snbt.substring(0, snbt.length() - 1)).formatted(NUMBER_COLOR));
			text.append(TextInst.literal(snbt.substring(snbt.length() - 1)).formatted(TYPE_SUFFIX_COLOR));
			return;
		}
		
		text.append(TextInst.literal(snbt).formatted(STRING_COLOR));
	}
	
	private void formatString(StringReader reader, EditableText text, Formatting color) {
		if (!StringReader.isQuotedStringStart(reader.peek())) {
			text.append(TextInst.literal(reader.readUnquotedString()).formatted(color));
			return;
		}
		
		int start = reader.getCursor();
		try {
			reader.readQuotedString();
		} catch (CommandSyntaxException e) {
			// Already handled by initial parse
			throw new IllegalArgumentException("Invalid string", e);
		}
		
		text.append(reader.getString().substring(start, start + 1));
		text.append(TextInst.literal(reader.getString().substring(start + 1, reader.getCursor() - 1)).formatted(color));
		text.append(reader.getString().substring(reader.getCursor() - 1, reader.getCursor()));
	}
	
	private void formatWhitespace(StringReader reader, EditableText text) {
		int start = reader.getCursor();
		reader.skipWhitespace();
		text.append(TextInst.of(reader.getString().substring(start, reader.getCursor())));
	}
	
	private void formatChar(StringReader reader, EditableText text) {
		text.append(String.valueOf(reader.read()));
	}
	
}
