package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.util.Identifier;

public class StatusEffectArgumentType implements ArgumentType<StatusEffect> {
	
	private static final Collection<String> EXAMPLES = Arrays.asList("spooky", "effect");
	private static final DynamicCommandExceptionType INVALID_EFFECT_EXCEPTION = new DynamicCommandExceptionType(
				id -> TextInst.translatable("effect.effectNotFound", id));
	
	public static StatusEffectArgumentType statusEffect() {
		return new StatusEffectArgumentType();
	}
	
	public StatusEffect parse(StringReader stringReader) throws CommandSyntaxException {
		Identifier id = Identifier.fromCommandInput(stringReader);
		return MVRegistry.STATUS_EFFECT.getOrEmpty(id).orElseThrow(() -> INVALID_EFFECT_EXCEPTION.create(id));
	}
	
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		return CommandSource.suggestIdentifiers(MVRegistry.STATUS_EFFECT.getIds(), builder);
	}
	
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
	
}
