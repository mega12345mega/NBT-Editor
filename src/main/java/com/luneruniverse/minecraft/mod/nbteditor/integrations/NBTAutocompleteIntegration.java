package com.luneruniverse.minecraft.mod.nbteditor.integrations;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestionManager;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

public class NBTAutocompleteIntegration extends Integration {
	
	public static final Optional<NBTAutocompleteIntegration> INSTANCE = Integration.getOptional(NBTAutocompleteIntegration::new);
	
	private static StringRange shiftRange(StringRange range, int shift) {
		return new StringRange(range.getStart() + shift, range.getEnd() + shift);
	}
	
	private NBTAutocompleteIntegration() {}
	
	@Override
	public String getModId() {
		return "nbt_ac";
	}
	
	public CompletableFuture<Suggestions> getSuggestions(ItemStack item, List<String> path, String key, String value, int cursor, Collection<String> otherTags) {
		if (value != null && otherTags != null)
			throw new IllegalArgumentException("Both value and otherTags can't be non-null at the same time!");
		
		boolean nextTagAllowed;
		if (value == null) {
			key = key.substring(0, cursor);
			nextTagAllowed = false;
		} else {
			nextTagAllowed = (cursor < value.length());
			value = value.substring(0, cursor);
		}
		
		if (key.contains(":") || key.contains("{") || key.contains("["))
			return new SuggestionsBuilder("", 0).buildFuture();
		
		StringBuilder pathBuilder = new StringBuilder();
		NbtElement nbt = item.getNbt();
		if (nbt != null) {
			for (String piece : path) {
				if (nbt instanceof NbtCompound compound) {
					pathBuilder.append('{');
					pathBuilder.append(piece);
					pathBuilder.append(':');
					nbt = compound.get(piece);
				} else if (nbt instanceof NbtList list) {
					pathBuilder.append('[');
					nbt = list.get(Integer.parseInt(piece));
				} else
					return new SuggestionsBuilder("", 0).buildFuture();
			}
		}
		if (nbt instanceof NbtCompound)
			pathBuilder.append('{');
		else
			return new SuggestionsBuilder("", 0).buildFuture();
		int fieldStart = pathBuilder.length();
		pathBuilder.append(key);
		if (value != null) {
			pathBuilder.append(':');
			fieldStart = pathBuilder.length();
			pathBuilder.append(value);
		}
		String pathStr = pathBuilder.toString();
		
		String suggestionId = "item/" + MVRegistry.ITEM.getId(item.getItem());
		SuggestionsBuilder builder = new SuggestionsBuilder(pathStr, 0);
		final int fieldStartFinal = fieldStart;
		final String valueFinal = value;
		return NbtSuggestionManager.loadFromName(suggestionId, pathStr, builder, false).thenApply(suggestions -> {
			List<Suggestion> shiftedSuggestions = suggestions.getList().stream()
					.filter(suggestion ->
							!suggestion.getText().isEmpty() &&
							!(valueFinal == null && suggestion.getText().contains(":")) &&
							!(!nextTagAllowed && (suggestion.getText().contains(",") || suggestion.getText().contains("}"))) &&
							!(otherTags != null && otherTags.contains(suggestion.getText())))
					.map(suggestion -> {
						Suggestion shiftedSuggestion = new Suggestion(shiftRange(suggestion.getRange(), -fieldStartFinal), suggestion.getText());
						NbtSuggestionManager.subtextMap.put(shiftedSuggestion, NbtSuggestionManager.subtextMap.remove(suggestion));
						return shiftedSuggestion;
					})
					.collect(Collectors.toList());
			return new Suggestions(shiftRange(suggestions.getRange(), -fieldStartFinal), shiftedSuggestions);
		});
	}
	public CompletableFuture<Suggestions> getSuggestions(ItemStack item, List<String> path, String key, String value, int cursor) {
		return getSuggestions(item, path, key, value, cursor, null);
	}
	
}
