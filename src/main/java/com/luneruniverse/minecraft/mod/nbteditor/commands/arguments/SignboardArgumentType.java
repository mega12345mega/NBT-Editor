package com.luneruniverse.minecraft.mod.nbteditor.commands.arguments;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionRegistry;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.item.Item;
import net.minecraft.item.SignItem;
import net.minecraft.util.Identifier;

public class SignboardArgumentType implements ArgumentType<SignItem> {
	
	private static final Collection<String> EXAMPLES = Arrays.asList("minecraft:oak_sign", "spruce_sign");
	
	public static SignboardArgumentType signboard() {
		return new SignboardArgumentType();
	}
	
	private final Map<Identifier, SignItem> signs;
	
	private SignboardArgumentType() {
		signs = new HashMap<>();
		for (Map.Entry<Identifier, Item> item : MultiVersionRegistry.ITEM.getEntrySet()) {
			if (item.getValue() instanceof SignItem sign)
				signs.put(item.getKey(), sign);
		}
	}
	
	public SignItem parse(StringReader stringReader) throws CommandSyntaxException {
		StringBuilder value = new StringBuilder();
		while (stringReader.canRead() && stringReader.peek() != ' ')
			value.append(stringReader.read());
		
		SignItem output = signs.get(new Identifier(value.toString()));
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
