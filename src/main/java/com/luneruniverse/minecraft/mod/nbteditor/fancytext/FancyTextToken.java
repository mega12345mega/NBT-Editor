package com.luneruniverse.minecraft.mod.nbteditor.fancytext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mojang.brigadier.StringReader;

import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

public record FancyTextToken(Type type, Optional<Object> content) {
	
	public enum Type {
		/**
		 * content: {@link String}
		 */
		TEXT,
		/**
		 * content: {@link TextColor}
		 */
		COLOR,
		/**
		 * content: {@link Formatting}
		 */
		FORMATTING,
		OPEN_PAREN,
		CLOSE_PAREN,
		OPEN_SQUARE,
		CLOSE_SQUARE,
		OPEN_CURLY,
		CLOSE_CURLY
	}
	
	public static List<FancyTextToken> parse(StringReader str) {
		List<FancyTextToken> output = new ArrayList<>();
		boolean escaped = false;
		int curlyLevel = 0;
		StringBuilder content = new StringBuilder();
		
		while (str.canRead()) {
			char c = str.read();
			if (escaped) {
				escaped = false;
			} else if (c == '\\') {
				escaped = true;
				continue;
			} else if (curlyLevel > 0) {
				if (c == '{')
					curlyLevel++;
				else if (c == '}')
					curlyLevel--;
				
				if (curlyLevel == 0) {
					if (!content.isEmpty()) {
						output.add(new FancyTextToken(Type.TEXT, content.toString()));
						content.setLength(0);
					}
					output.add(new FancyTextToken(Type.CLOSE_CURLY));
					continue;
				}
			} else if (c == '&' || c == '§' || c == '[' || c == ']' || c == '(' || c == ')' || c == '{' || c == '}') {
				if (!content.isEmpty()) {
					output.add(new FancyTextToken(Type.TEXT, content.toString()));
					content.setLength(0);
				}
				
				if (c == '&' || c == '§') {
					if (!str.canRead()) {
						content.append(c);
						continue;
					}
					char c2 = str.peek();
					if (c2 == '#') {
						if (!str.canRead(6)) {
							content.append(c);
							continue;
						}
						for (int i = 0; i < 6; i++)
							content.append(str.peek(i + 1));
						try {
							output.add(new FancyTextToken(Type.COLOR, TextColor.fromRgb(Integer.parseInt(content.toString(), 16))));
							content.setLength(0);
							str.setCursor(str.getCursor() + 7);
						} catch (NumberFormatException e) {
							content.setLength(0);
							content.append(c);
							continue;
						}
					} else {
						Formatting formatting = Formatting.byCode(c2);
						if (formatting == null) {
							content.append(c);
							continue;
						} else {
							output.add(new FancyTextToken(Type.FORMATTING, formatting));
							str.skip();
						}
					}
				} else {
					output.add(new FancyTextToken(switch (c) {
						case '(' -> Type.OPEN_PAREN;
						case ')' -> Type.CLOSE_PAREN;
						case '[' -> Type.OPEN_SQUARE;
						case ']' -> Type.CLOSE_SQUARE;
						case '{' -> Type.OPEN_CURLY;
						case '}' -> Type.CLOSE_CURLY;
						default -> null; // Impossible
					}));
					if (c == '{')
						curlyLevel++;
				}
				
				continue;
			}
			content.append(c);
		}
		
		if (!content.isEmpty())
			output.add(new FancyTextToken(Type.TEXT, content.toString()));
		
		return output;
	}
	
	public FancyTextToken(Type type) {
		this(type, Optional.empty());
	}
	
	public FancyTextToken(Type type, Object content) {
		this(type, Optional.of(content));
	}
	
}
