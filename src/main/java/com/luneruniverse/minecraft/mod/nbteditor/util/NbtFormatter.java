package com.luneruniverse.minecraft.mod.nbteditor.util;

import java.util.Map;
import java.util.regex.Pattern;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.EditableText;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtType;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class NbtFormatter {
	
	public static record FormatterResult(Text text, boolean isSuccess) {}
	
	@FunctionalInterface
	public interface Impl {
		Text format(String str) throws CommandSyntaxException;
		default FormatterResult formatSafely(String str) {
			try {
				return new FormatterResult(format(str), true);
			} catch (Exception e) {
				return new FormatterResult(TextInst.literal(str).formatted(Formatting.RED), false);
			}
		}
	}
	
	public static Impl FORMATTER = NbtFormatter::formatElement;
	
	
	private static final SimpleCommandExceptionType TRAILING_DATA = new SimpleCommandExceptionType(TextInst.translatable("argument.nbt.trailing"));
	private static final SimpleCommandExceptionType EXPECTED_KEY = new SimpleCommandExceptionType(TextInst.translatable("argument.nbt.expected.key"));
	private static final SimpleCommandExceptionType EXPECTED_VALUE = new SimpleCommandExceptionType(TextInst.translatable("argument.nbt.expected.value"));
	private static final DynamicCommandExceptionType ARRAY_INVALID = new DynamicCommandExceptionType(type -> TextInst.translatable("argument.nbt.array.invalid", type));
    private static final Pattern DOUBLE_PATTERN_IMPLICIT = Pattern.compile("[-+]?(?:[0-9]+[.]|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?", 2);
    private static final Pattern DOUBLE_PATTERN = Pattern.compile("[-+]?(?:[0-9]+[.]?|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?d", 2);
    private static final Pattern FLOAT_PATTERN = Pattern.compile("[-+]?(?:[0-9]+[.]?|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?f", 2);
    private static final Pattern BYTE_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)b", 2);
    private static final Pattern LONG_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)l", 2);
    private static final Pattern SHORT_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)s", 2);
    private static final Pattern INT_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)");
	private static final Formatting NAME_COLOR = Formatting.AQUA;
	private static final Formatting STRING_COLOR = Formatting.GREEN;
	private static final Formatting NUMBER_COLOR = Formatting.GOLD;
	private static final Formatting TYPE_SUFFIX_COLOR = Formatting.RED;
	
	
	
	public static Text formatElement(StringReader reader) throws CommandSyntaxException {
		// Check list types
		int cursor = reader.getCursor();
		new StringNbtReader(reader).parseElement();
		reader.setCursor(cursor);
		
		// Format
		NbtFormatter formatter = new NbtFormatter(reader);
		EditableText output = formatter.parseElement();
		output.append(formatter.skipWhitespace());
		if (reader.canRead())
			throw TRAILING_DATA.createWithContext(reader);
		return output;
	}
	public static Text formatElement(String str) throws CommandSyntaxException {
		return formatElement(new StringReader(str));
	}
	
	
	private StringReader reader;
	
	private NbtFormatter(StringReader reader) {
		this.reader = reader;
	}
	
	private EditableText skipWhitespace() {
		StringBuilder output = new StringBuilder();
		while (reader.canRead() && Character.isWhitespace(reader.peek()))
			output.append(reader.read());
		return TextInst.literal(output.toString());
	}
	
	private String readStringUntil(char terminator) throws CommandSyntaxException {
		final StringBuilder result = new StringBuilder();
		boolean escaped = false;
		while (reader.canRead()) {
			final char c = reader.read();
			if (escaped) {
				if (c == terminator || c == '\\') {
					result.append(c);
					escaped = false;
				} else {
					reader.setCursor(reader.getCursor() - 1);
					throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidEscape().createWithContext(reader,
							String.valueOf(c));
				}
			} else if (c == '\\') {
				escaped = true;
				result.append(c);
			} else if (c == terminator) {
				return result.toString();
			} else {
				result.append(c);
			}
		}
		
		throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedEndOfQuote().createWithContext(reader);
	}
	
	private String readString() throws CommandSyntaxException {
		if (!reader.canRead()) {
			return "";
		}
		final char next = reader.peek();
		if (StringReader.isQuotedStringStart(next)) {
			reader.skip();
			return next + readStringUntil(next) + next;
		}
		return reader.readUnquotedString();
	}
	
	private EditableText readString(Formatting color) throws CommandSyntaxException {
		EditableText output = TextInst.literal("");
		output.append(this.skipWhitespace());
        if (!this.reader.canRead()) {
            throw EXPECTED_KEY.createWithContext(this.reader);
        }
        String str = this.readString();
        if (str.isEmpty())
        	return null;
        output.append(TextInst.literal(str).formatted(color));
        return output;
	}
	
	private EditableText readQuotedString() throws CommandSyntaxException {
		EditableText output = TextInst.literal("");
		if (!reader.canRead()) {
			return output;
		}
		final char next = reader.peek();
		if (!StringReader.isQuotedStringStart(next)) {
			throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedStartOfQuote().createWithContext(reader);
		}
		reader.skip();
		output.append(TextInst.literal(this.readStringUntil(next)));
		return output;
	}
	
	private Map.Entry<Boolean, EditableText> readComma() {
		EditableText output = TextInst.literal("");
		output.append(this.skipWhitespace());
		if (this.reader.canRead() && this.reader.peek() == ',') {
			output.append(TextInst.literal(this.reader.read() + ""));
			output.append(this.skipWhitespace());
			return Map.entry(true, output);
		} else
			return Map.entry(false, output);
	}
	
	private EditableText readArray(NbtType<?> arrayTypeReader, NbtType<?> typeReader) throws CommandSyntaxException {
		EditableText output = TextInst.literal("");
		while (this.reader.peek() != ']') {
			output.append(this.parseElement());
			Map.Entry<Boolean, EditableText> comma = this.readComma();
			output.append(comma.getValue());
			if (!comma.getKey())
				break;
			if (this.reader.canRead())
				continue;
			throw EXPECTED_VALUE.createWithContext(this.reader);
		}
		output.append(this.expect(']'));
		return output;
	}
	
	private EditableText parseElement() throws CommandSyntaxException {
		EditableText output = TextInst.literal("");
		output.append(skipWhitespace());
		if (!this.reader.canRead()) {
			throw EXPECTED_VALUE.createWithContext(this.reader);
		}
		char c = this.reader.peek();
		if (c == '{') {
			output.append(this.parseCompound());
		} else if (c == '[') {
			output.append(this.parseArray());
		} else {
			output.append(this.parseElementPrimitive());
		}
		return output;
	}
	
	private EditableText parseCompound() throws CommandSyntaxException {
		EditableText output = TextInst.literal("");
		output.append(this.expect('{'));
		this.reader.skipWhitespace();
		while (this.reader.canRead() && this.reader.peek() != '}') {
			int i = this.reader.getCursor();
			EditableText string = this.readString(NAME_COLOR);
			if (string == null) {
				this.reader.setCursor(i);
				throw EXPECTED_KEY.createWithContext(this.reader);
			}
			output.append(string);
			output.append(this.expect(':'));
			output.append(this.parseElement());
			Map.Entry<Boolean, EditableText> comma = this.readComma();
			output.append(comma.getValue());
			if (!comma.getKey())
				break;
			if (this.reader.canRead())
				continue;
			throw EXPECTED_KEY.createWithContext(this.reader);
		}
		output.append(this.expect('}'));
		return output;
	}
	
	private EditableText parseArray() throws CommandSyntaxException {
		if (this.reader.canRead(3) && !StringReader.isQuotedStringStart(this.reader.peek(1))
				&& this.reader.peek(2) == ';') {
			return this.parseElementPrimitiveArray();
		}
		return this.parseList();
	}
	
	private EditableText parseElementPrimitiveArray() throws CommandSyntaxException {
		EditableText output = TextInst.literal("");
		output.append(this.expect('['));
		int i = this.reader.getCursor();
		char c = this.reader.read();
		output.append(TextInst.literal(c + "").formatted(TYPE_SUFFIX_COLOR));
		output.append(TextInst.literal(this.reader.read() + ""));
		output.append(this.skipWhitespace());
		if (!this.reader.canRead()) {
			throw EXPECTED_VALUE.createWithContext(this.reader);
		}
		if (c == 'B') {
			output.append(this.readArray(NbtByteArray.TYPE, NbtByte.TYPE));
			return output;
		}
		if (c == 'L') {
			output.append(this.readArray(NbtLongArray.TYPE, NbtLong.TYPE));
			return output;
		}
		if (c == 'I') {
			output.append(this.readArray(NbtIntArray.TYPE, NbtInt.TYPE));
			return output;
		}
		this.reader.setCursor(i);
		throw ARRAY_INVALID.createWithContext(this.reader, String.valueOf(c));
	}
	
	private EditableText parseList() throws CommandSyntaxException {
		EditableText output = TextInst.literal("");
		output.append(this.expect('['));
		output.append(this.skipWhitespace());
		if (!this.reader.canRead()) {
			throw EXPECTED_VALUE.createWithContext(this.reader);
		}
		while (this.reader.peek() != ']') {
			EditableText nbtElement = this.parseElement();
			output.append(nbtElement);
			Map.Entry<Boolean, EditableText> comma = this.readComma();
			output.append(comma.getValue());
			if (!comma.getKey())
				break;
			if (this.reader.canRead())
				continue;
			throw EXPECTED_VALUE.createWithContext(this.reader);
		}
		output.append(this.expect(']'));
		return output;
	}
	
	private EditableText parseElementPrimitive() throws CommandSyntaxException {
		EditableText output = TextInst.literal("");
		output.append(this.skipWhitespace());
		int i = this.reader.getCursor();
		char quote = this.reader.peek();
		if (StringReader.isQuotedStringStart(quote)) {
			output.append(TextInst.literal(quote + "").formatted(STRING_COLOR))
					.append(this.readQuotedString().formatted(STRING_COLOR))
					.append(TextInst.literal(quote + "").formatted(STRING_COLOR));
			return output;
		}
		String string = this.reader.readUnquotedString();
		if (string.isEmpty()) {
			this.reader.setCursor(i);
			throw EXPECTED_VALUE.createWithContext(this.reader);
		}
		return this.parsePrimitive(string);
	}
	
	private EditableText parsePrimitive(String input) {
		try {
			EditableText numberInput = input.isEmpty() ? TextInst.literal("") :
				TextInst.literal(input.substring(0, input.length() - 1)).formatted(NUMBER_COLOR)
					.append(TextInst.literal(input.substring(input.length() - 1)).formatted(TYPE_SUFFIX_COLOR));
			if (FLOAT_PATTERN.matcher(input).matches()) {
				return numberInput;
			}
			if (BYTE_PATTERN.matcher(input).matches()) {
				return numberInput;
			}
			if (LONG_PATTERN.matcher(input).matches()) {
				return numberInput;
			}
			if (SHORT_PATTERN.matcher(input).matches()) {
				return numberInput;
			}
			if (INT_PATTERN.matcher(input).matches()) {
				return TextInst.literal(input).formatted(NUMBER_COLOR);
			}
			if (DOUBLE_PATTERN.matcher(input).matches()) {
				return numberInput;
			}
			if (DOUBLE_PATTERN_IMPLICIT.matcher(input).matches()) {
				return TextInst.literal(input).formatted(NUMBER_COLOR);
			}
			if ("true".equalsIgnoreCase(input)) {
				return TextInst.literal(input).formatted(NUMBER_COLOR);
			}
			if ("false".equalsIgnoreCase(input)) {
				return TextInst.literal(input).formatted(NUMBER_COLOR);
			}
		} catch (NumberFormatException numberFormatException) {
			// empty catch block
		}
		return TextInst.literal(input).formatted(STRING_COLOR);
	}
	
	private EditableText expect(char c) throws CommandSyntaxException {
		EditableText output = TextInst.literal("");
		output.append(skipWhitespace());
		this.reader.expect(c);
		output.append(TextInst.literal(c + ""));
		return output;
	}
	
}
