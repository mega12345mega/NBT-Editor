package com.luneruniverse.minecraft.mod.nbteditor.commands.arguments;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;

public class SummonableEntityArgumentType implements ArgumentType<Identifier> {
	private static final Collection<String> EXAMPLES = Arrays.asList("minecraft:pig", "cow");
	
	public static SummonableEntityArgumentType summonableEntity() {
		return new SummonableEntityArgumentType();
	}
	
	public Identifier parse(StringReader stringReader) throws CommandSyntaxException {
		Identifier id = Identifier.fromCommandInput(stringReader);
		MVRegistry.ENTITY_TYPE.getOrEmpty(id).filter(EntityType::isSummonable).orElseThrow(
				() -> CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(stringReader));
		return id;
	}
	
	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		return CommandSource.suggestIdentifiers(MVRegistry.ENTITY_TYPE.getEntrySet().stream()
				.filter(entry -> entry.getValue().isSummonable()).map(Map.Entry::getKey), builder);
	}
	
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
}