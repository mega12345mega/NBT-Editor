package com.luneruniverse.minecraft.mod.nbteditor.snbtformatters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.EditableText;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.mojang.brigadier.StringReader;

import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.SnbtParsing;
import net.minecraft.util.Formatting;
import net.minecraft.util.packrat.PackratParser;
import net.minecraft.util.packrat.ParseErrorList;
import net.minecraft.util.packrat.ParsingRuleEntry;
import net.minecraft.util.packrat.ReaderBackedParsingState;
import net.minecraft.util.packrat.Symbol;

public class NormalSNBTFormatter2 extends SNBTFormatter {
	
	private static record ParsedSection(int start, int end, Symbol<?> symbol) {}
	
	private static final ParseErrorList<StringReader> ERROR_LIST = new ParseErrorList.Noop<>();
	private static final PackratParser<NbtElement> PARSER = SnbtParsing.createParser(NbtOps.INSTANCE);
	
	@Override
	public Result format(String snbt, boolean allowSpecialNumbers) {
		List<ParsedSection> parsedSections = new ArrayList<>();
		
		StringReader reader = new StringReader(snbt);
		ReaderBackedParsingState parsingState = new ReaderBackedParsingState(ERROR_LIST, reader) {
			@Override
			public <T> T parse(ParsingRuleEntry<StringReader, T> rule) {
				int parsedSectionsMark = parsedSections.size();
				int startCursor = getCursor();
				
				T parsed = super.parse(rule);
				
				if (parsed == null)
					parsedSections.subList(parsedSectionsMark, parsedSections.size()).clear();
				else
					parsedSections.add(new ParsedSection(startCursor, getCursor(), rule.getSymbol()));
				
				return parsed;
			}
		};
		
		Optional<NbtElement> nbt;
		if (allowSpecialNumbers)
			MixinLink.specialNumbers.add(Thread.currentThread());
		try {
			nbt = PARSER.startParsing(parsingState);
		} finally {
			if (allowSpecialNumbers)
				MixinLink.specialNumbers.remove(Thread.currentThread());
		}
		
		if (nbt.isEmpty())
			return Result.ofFailure(snbt);
		
		reader.skipWhitespace();
		if (reader.canRead())
			return Result.ofFailure(snbt);
		
		TreeMap<Integer, Formatting> formattingSections = new TreeMap<>();
		
		for (ParsedSection parsedSection : parsedSections) {
			Formatting formatting = switch (parsedSection.symbol().name()) {
				case "integer_literal", "float_literal" -> NUMBER_COLOR;
				case "integer_suffix", "float_type_suffix", "array_prefix" -> TYPE_SUFFIX_COLOR;
				case "unquoted_string" -> {
					String value = snbt.substring(parsedSection.start(), parsedSection.end()).stripLeading();
					if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"))
						yield NUMBER_COLOR;
					if (allowSpecialNumbers && SPECIAL_NUMS.containsKey(value)) {
						formattingSections.put(parsedSection.end() - 1, TYPE_SUFFIX_COLOR);
						yield NUMBER_COLOR;
					}
					yield STRING_COLOR;
				}
				case "single_quoted_string_contents", "double_quoted_string_contents" -> STRING_COLOR;
				case "arguments" -> {
					formattingSections.put(formattingSections.floorKey(parsedSection.start() - 2), OPERATOR_COLOR);
					yield null;
				}
				case "map_key" -> {
					formattingSections.put(formattingSections.ceilingKey(parsedSection.start()), NAME_COLOR);
					yield null;
				}
				default -> null;
			};
			if (formatting == null)
				continue;
			
			formattingSections.put(parsedSection.start(), formatting);
			formattingSections.put(parsedSection.end(), null);
		}
		formattingSections.put(snbt.length(), null);
		
		EditableText output = TextInst.literal("");
		
		int start = 0;
		Formatting nextFormatting = null;
		for (Map.Entry<Integer, Formatting> formattingSection : formattingSections.entrySet()) {
			int end = formattingSection.getKey();
			Formatting formatting = formattingSection.getValue();
			
			EditableText section = TextInst.literal(snbt.substring(start, end));
			if (nextFormatting != null)
				section = section.formatted(nextFormatting);
			output.append(section);
			
			start = end;
			nextFormatting = formatting;
		}
		
		return Result.ofSuccess(output, nbt.get());
	}
	
}
