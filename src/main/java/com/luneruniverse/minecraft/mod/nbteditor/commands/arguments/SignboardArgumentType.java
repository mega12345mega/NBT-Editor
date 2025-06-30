package com.luneruniverse.minecraft.mod.nbteditor.commands.arguments;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IdentifierInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

public class SignboardArgumentType implements ArgumentType<Item> {
	
	private static final Collection<String> EXAMPLES = Arrays.asList("minecraft:oak_sign", "spruce_sign");
	
	public static SignboardArgumentType signboard() {
		return new SignboardArgumentType();
	}
	
	private final Map<Identifier, Item> signs;
	
	private SignboardArgumentType() {
		signs = new HashMap<>();
		for (Map.Entry<Identifier, Item> item : MVRegistry.ITEM.getEntrySet()) {
			if (MVMisc.isSignItem(item.getValue()))
				signs.put(item.getKey(), item.getValue());
		}
	}
	
	public Item parse(StringReader stringReader) throws CommandSyntaxException {
		StringBuilder value = new StringBuilder();
		while (stringReader.canRead() && stringReader.peek() != ' ')
			value.append(stringReader.read());
		
		Item output = signs.get(IdentifierInst.of(value.toString()));
		if (output == null)
			throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(stringReader);
		return output;
	}
	
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		return CommandSource.suggestIdentifiers(signs.keySet(), builder);
	}
	
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
	
}
