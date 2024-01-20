package com.luneruniverse.minecraft.mod.nbteditor.integrations;

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
	
	public CompletableFuture<Suggestions> getSuggestions(ItemStack item, List<String> path, String key, String value) {
		if (key.contains(":") || key.contains("{") || key.contains("["))
			return new SuggestionsBuilder("", 0).buildFuture();
		
		StringBuilder pathBuilder = new StringBuilder("{");
		NbtElement nbt = item.getNbt();
		if (nbt != null) {
			for (String piece : path) {
				if (nbt instanceof NbtCompound compound) {
					pathBuilder.append(piece);
					pathBuilder.append(":{");
					nbt = compound.get(piece);
				} else if (nbt instanceof NbtList list) {
					pathBuilder.append('[');
					nbt = list.get(Integer.parseInt(piece));
				} else
					return new SuggestionsBuilder("", 0).buildFuture();
			}
		}
		if (!(nbt instanceof NbtCompound))
			return new SuggestionsBuilder("", 0).buildFuture();
		int fieldStart = pathBuilder.length();
		pathBuilder.append(key);
		if (value != null) {
			pathBuilder.append(':');
			fieldStart = pathBuilder.length();
			pathBuilder.append(value);
		}
		final int fieldStartFinal = fieldStart;
		String pathStr = pathBuilder.toString();
		
		String suggestionId = "item/" + MVRegistry.ITEM.getId(item.getItem());
		SuggestionsBuilder builder = new SuggestionsBuilder(pathStr, 0);
		return NbtSuggestionManager.loadFromName(suggestionId, pathStr, builder, false).thenApply(suggestions -> {
			List<Suggestion> movedSuggestions = suggestions.getList().stream()
					.filter(suggestion -> value == null && !suggestion.getText().contains(":"))
					.map(suggestion -> new Suggestion(shiftRange(suggestion.getRange(), -fieldStartFinal), suggestion.getText()))
					.collect(Collectors.toList());
			return new Suggestions(shiftRange(suggestions.getRange(), -fieldStartFinal), movedSuggestions);
		});
	}
	
}
