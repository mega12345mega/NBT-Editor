package com.luneruniverse.minecraft.mod.nbteditor.commands.arguments;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.command.ServerCommandSource;

public class EnumArgumentType<T extends Enum<T>> implements ArgumentType<T> {
	
	private static final Collection<String> EXAMPLES = Arrays.asList("option1", "option2");
	
	public static <T extends Enum<T>> EnumArgumentType<T> options(Class<T> options) {
		return new EnumArgumentType<>(options);
	}

	@SuppressWarnings("unchecked")
	public static Collection<StatusEffectInstance> getStatusEffectInstance(CommandContext<ServerCommandSource> context, String name) {
		return context.getArgument(name, Collection.class);
	}
	
	private final Class<T> options;
	
	private EnumArgumentType(Class<T> options) {
		this.options = options;
	}
	
	public T parse(StringReader stringReader) throws CommandSyntaxException {
		StringBuilder value = new StringBuilder();
		while (stringReader.canRead() && stringReader.peek() != ' ')
			value.append(stringReader.read());
		return Arrays.stream(options.getEnumConstants()).filter(option -> option.name().equalsIgnoreCase(value.toString())).findFirst().orElse(null);
	}

	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		return CommandSource.suggestMatching(Arrays.stream(options.getEnumConstants()).map(T::name).map(String::toLowerCase), builder);
	}

	public Collection<String> getExamples() {
		return EXAMPLES;
	}
}
