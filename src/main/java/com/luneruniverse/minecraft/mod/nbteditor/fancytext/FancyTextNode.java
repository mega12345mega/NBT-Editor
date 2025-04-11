package com.luneruniverse.minecraft.mod.nbteditor.fancytext;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

public interface FancyTextNode {
	
	public static List<FancyTextNode> parse(List<FancyTextToken> tokens) {
		return parse(tokens, null);
	}
	
	private static List<FancyTextNode> parse(List<FancyTextToken> tokens, AtomicInteger internalParseIndex) {
		List<FancyTextNode> output = new ArrayList<>();
		int parenLevel = 0;
		
		loop: for (int i = (internalParseIndex == null ? 0 : internalParseIndex.getPlain()); i < tokens.size(); i++) {
			FancyTextToken token = tokens.get(i);
			switch (token.type()) {
				case TEXT -> output.add(new FancyTextTextNode((String) token.content().get()));
				case COLOR -> output.add(new FancyTextColorNode((TextColor) token.content().get()));
				case FORMATTING -> output.add(new FancyTextFormattingNode((Formatting) token.content().get()));
				case OPEN_PAREN -> {
					parenLevel++;
					output.add(new FancyTextTextNode("("));
				}
				case CLOSE_PAREN -> {
					if (internalParseIndex == null || parenLevel > 0) {
						if (parenLevel > 0)
							parenLevel--;
						output.add(new FancyTextTextNode(")"));
					} else {
						internalParseIndex.setPlain(i);
						break loop;
					}
				}
				case OPEN_SQUARE -> {
					boolean success = false;
					if (i + 4 < tokens.size()) {
						FancyTextToken event = tokens.get(i + 1);
						FancyTextToken closeSquare = tokens.get(i + 2);
						if (event.type() == FancyTextToken.Type.TEXT && closeSquare.type() == FancyTextToken.Type.CLOSE_SQUARE) {
							TextAction action = TextAction.valueOf(((String) event.content().get()).toUpperCase());
							if (action != null) {
								int openIndex = i + 3;
								FancyTextToken open = tokens.get(openIndex);
								String value = null;
								if (open.type() == FancyTextToken.Type.OPEN_CURLY && i + 6 < tokens.size()) {
									FancyTextToken valueToken = tokens.get(i + 4);
									FancyTextToken closeCurly = tokens.get(i + 5);
									if (valueToken.type() == FancyTextToken.Type.CLOSE_CURLY) {
										value = "";
										openIndex = i + 5;
										open = closeCurly;
									} else if (valueToken.type() == FancyTextToken.Type.TEXT && closeCurly.type() == FancyTextToken.Type.CLOSE_CURLY) {
										value = (String) valueToken.content().get();
										openIndex = i + 6;
										open = tokens.get(i + 6);
									}
								}
								if (open.type() == FancyTextToken.Type.OPEN_PAREN) {
									AtomicInteger internalParseIndex2 = new AtomicInteger(openIndex + 1);
									List<FancyTextNode> contents = parse(tokens, internalParseIndex2);
									i = internalParseIndex2.getPlain();
									output.add(new FancyTextEventNode(action, value, contents));
									success = true;
								}
							}
						}
					}
					if (!success)
						output.add(new FancyTextTextNode("["));
				}
				case CLOSE_SQUARE -> output.add(new FancyTextTextNode("]"));
				case OPEN_CURLY -> output.add(new FancyTextTextNode("{"));
				case CLOSE_CURLY -> output.add(new FancyTextTextNode("}"));
			}
		}
		
		FancyTextTextNode prevText = null;
		for (ListIterator<FancyTextNode> i = output.listIterator(); i.hasNext();) {
			FancyTextNode node = i.next();
			if (node instanceof FancyTextTextNode text) {
				if (prevText == null)
					prevText = text;
				else {
					prevText = new FancyTextTextNode(prevText.text() + text.text());
					i.remove();
					i.previous();
					i.set(prevText);
					i.next();
				}
			} else
				prevText = null;
		}
		
		return output;
	}
	
	public Style modifyStyle(Style style);
	public int getNumberOfTextNodes();
	
}
