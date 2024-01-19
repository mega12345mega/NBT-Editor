package com.luneruniverse.minecraft.mod.nbteditor.commands.arguments;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.command.CommandSource;

public class ClientChestPageNameArgumentType implements ArgumentType<String> {
	
	private static final Collection<String> EXAMPLES = Arrays.asList("Swords", "Potions");
	
	public static ClientChestPageNameArgumentType pageName() {
		return new ClientChestPageNameArgumentType();
	}
	
	private ClientChestPageNameArgumentType() {}
	
	public String parse(StringReader stringReader) throws CommandSyntaxException {
		String text = stringReader.getRemaining();
		stringReader.setCursor(stringReader.getTotalLength());
		return text;
	}
	
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		return CommandSource.suggestMatching(NBTEditorClient.CLIENT_CHEST.getAllPageNames(), builder);
	}
	
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
	
}
