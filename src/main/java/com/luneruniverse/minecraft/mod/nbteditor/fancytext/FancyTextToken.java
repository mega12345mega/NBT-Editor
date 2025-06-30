package com.luneruniverse.minecraft.mod.nbteditor.fancytext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.util.StyleUtil;
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
		 * content: {@link Formatting} <br>
		 * (May be formatting for color)
		 */
		FORMATTING,
		/**
		 * content: {@link Integer}
		 */
		SHADOW_COLOR,
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
					int startCursor = str.getCursor();
					
					char c2 = str.read();
					boolean shadow = false;
					if (c2 == '_' && str.canRead() && StyleUtil.SHADOW_COLOR_EXISTS) {
						shadow = true;
						c2 = str.read();
					}
					
					if (c2 == '#') {
						if (!str.canRead(7)) {
							content.append(c);
							str.setCursor(startCursor);
							continue;
						}
						
						for (int i = 0; i < 6; i++)
							content.append(str.read());
						if (shadow && str.canRead(3) && str.peek() != ';') {
							content.append(str.read());
							content.append(str.read());
						}
						
						try {
							if (str.read() != ';')
								throw new NumberFormatException();
							int color = Integer.parseUnsignedInt(content.toString(), 16);
							if (shadow) {
								output.add(new FancyTextToken(Type.SHADOW_COLOR,
										content.length() == 6 ? color | 0xFF000000 : color));
							} else
								output.add(new FancyTextToken(Type.COLOR, TextColor.fromRgb(color)));
							content.setLength(0);
						} catch (NumberFormatException e) {
							content.setLength(0);
							content.append(c);
							str.setCursor(startCursor);
						}
					} else {
						Formatting formatting = Formatting.byCode(c2);
						if (formatting == null || shadow && !formatting.isColor()) {
							content.append(c);
							str.setCursor(startCursor);
						} else {
							if (shadow) {
									output.add(new FancyTextToken(Type.SHADOW_COLOR,
											MVMisc.scaleRgb(formatting.getColorValue(), 0.25) | 0xFF000000));
							} else
								output.add(new FancyTextToken(Type.FORMATTING, formatting));
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
