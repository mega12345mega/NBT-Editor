package com.luneruniverse.minecraft.mod.nbteditor.integrations;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalBlock;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalEntity;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalItem;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalNBT;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.manager.NBTManagers;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.registry.DynamicRegistryManagerHolder;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestionManager;

import net.minecraft.command.argument.ItemStringReader;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;

public class NBTAutocompleteIntegration extends Integration {
	
	public static final Optional<NBTAutocompleteIntegration> INSTANCE = Integration.getOptional(NBTAutocompleteIntegration::new);
	
	private static StringRange shiftRange(StringRange range, int shift) {
		return new StringRange(range.getStart() + shift, range.getEnd() + shift);
	}
	private static Suggestion shiftSuggestion(Suggestion suggestion, int shift) {
		return replaceSuggestion(suggestion,
				new Suggestion(shiftRange(suggestion.getRange(), shift), suggestion.getText(), suggestion.getTooltip()));
	}
	private static Suggestion replaceSuggestion(Suggestion oldSuggestion, Suggestion newSuggestion) {
		NbtSuggestionManager.subtextMap.put(newSuggestion, NbtSuggestionManager.subtextMap.remove(oldSuggestion));
		return newSuggestion;
	}
	
	private NBTAutocompleteIntegration() {}
	
	@Override
	public String getModId() {
		return "nbt_ac";
	}
	
	private CompletableFuture<Suggestions> getSuggestions(String type, Identifier id, NbtElement nbt, List<String> path, String key, String value, int cursor, Collection<String> otherTags) {
		if (value != null && otherTags != null)
			throw new IllegalArgumentException("Both value and otherTags can't be non-null at the same time!");
		if (key == null && value == null)
			throw new IllegalArgumentException("Both key and value can't be null at the same time!");
		
		boolean components = NBTManagers.COMPONENTS_EXIST && type.equals("item");
		
		boolean nextTagAllowed;
		if (value == null) {
			key = key.substring(0, cursor);
			nextTagAllowed = false;
		} else {
			nextTagAllowed = (cursor < value.length());
			value = value.substring(0, cursor);
		}
		
		if (key != null && (key.contains("{") || key.contains("[")))
			return new SuggestionsBuilder("", 0).buildFuture();
		
		StringBuilder pathBuilder = new StringBuilder();
		boolean firstKey = true;
		if (nbt != null) {
			for (String piece : path) {
				if (nbt instanceof NbtCompound compound) {
					if (firstKey && components) {
						pathBuilder.append('[');
						pathBuilder.append(piece);
						pathBuilder.append('=');
					} else {
						pathBuilder.append('{');
						pathBuilder.append(escapeKey(piece));
						pathBuilder.append(':');
					}
					nbt = compound.get(piece);
				} else if (nbt instanceof NbtList list) {
					pathBuilder.append('[');
					nbt = list.get(Integer.parseInt(piece));
				} else
					return new SuggestionsBuilder("", 0).buildFuture();
				firstKey = false;
			}
		}
		int fieldStart = pathBuilder.length();
		if (key != null) {
			if (nbt instanceof NbtCompound) {
				if (firstKey && components)
					pathBuilder.append('[');
				else
					pathBuilder.append('{');
			} else if (nbt instanceof NbtList)
				pathBuilder.append('[');
			else
				return new SuggestionsBuilder("", 0).buildFuture();
			fieldStart = pathBuilder.length();
			
			if (nbt instanceof NbtCompound) {
				if (firstKey && components)
					pathBuilder.append(key);
				else {
					String escapedKey = escapeKey(key);
					if (key.equals(escapedKey))
						pathBuilder.append(key);
					else if (value == null)
						pathBuilder.append(escapedKey.substring(0, escapedKey.length() - 1));
					else
						pathBuilder.append(escapedKey);
				}
			}
			
			if (value != null) {
				if (nbt instanceof NbtCompound) {
					if (firstKey && components)
						pathBuilder.append('=');
					else
						pathBuilder.append(':');
					fieldStart = pathBuilder.length();
				}
				pathBuilder.append(value);
			}
		} else {
			if (firstKey && components) {
				pathBuilder.append("[container=[{item:{id:\"" + id + "\",components:");
				fieldStart = pathBuilder.length();
			}
			pathBuilder.append(value);
		}
		String pathStr = pathBuilder.toString();
		
		String suggestionId = type + "/" + id;
		final int fieldStartFinal = fieldStart;
		final String valueFinal = value;
		final boolean firstKeyFinal = firstKey;
		return loadFromName(suggestionId, pathStr, components).thenApply(suggestions -> {
			List<Suggestion> shiftedSuggestions = suggestions.getList().stream()
					.filter(suggestion ->
							!(suggestion.getText().isEmpty()) &&
							!(valueFinal == null && suggestion.getText().contains(":")) &&
							!(valueFinal == null && firstKeyFinal && components && suggestion.getText().contains("{")) &&
							!(!nextTagAllowed && (suggestion.getText().contains(",") || suggestion.getText().contains("}"))) &&
							!(otherTags != null && otherTags.contains(suggestion.getText())))
					.map(suggestion -> {
						suggestion = shiftSuggestion(suggestion, -fieldStartFinal);
						if (firstKeyFinal && components) {
							if (!suggestion.getText().equals("!")) {
								String component = suggestion.getText();
								boolean extraEquals = component.endsWith("=");
								if (extraEquals)
									component = component.substring(0, component.length() - 1);
								if (otherTags != null &&
										(otherTags.contains("minecraft:" + component) || otherTags.contains("!minecraft:" + component) ||
										otherTags.contains(":" + component) || otherTags.contains("!:" + component) ||
										otherTags.contains(component) || otherTags.contains("!" + component))) {
									return null;
								}
								if (extraEquals) {
									suggestion = replaceSuggestion(suggestion,
											new Suggestion(suggestion.getRange(), component, suggestion.getTooltip()));
								}
							}
						}
						return suggestion;
					})
					.filter(suggestion -> suggestion != null)
					.collect(Collectors.toList());
			return new Suggestions(shiftRange(suggestions.getRange(), -fieldStartFinal), shiftedSuggestions);
		});
	}
	private String escapeKey(String key) {
		if (key.isEmpty() || MVMisc.isSimpleName(key))
			return key;
		return NbtString.escape(key);
	}
	private CompletableFuture<Suggestions> loadFromName(String name, String tag, boolean components) {
		if (components) {
			name = name.substring("item/".length());
			int shift = name.length();
			SuggestionsBuilder builder = new SuggestionsBuilder(name + tag, 0);
			return new ItemStringReader(DynamicRegistryManagerHolder.get()).getSuggestions(builder).thenApply(suggestions -> {
				return new Suggestions(shiftRange(suggestions.getRange(), -shift), suggestions.getList().stream()
						.map(suggestion -> shiftSuggestion(suggestion, -shift)).collect(Collectors.toList()));
			});
		}
		return NbtSuggestionManager.loadFromName(name, tag, new SuggestionsBuilder(tag, 0), false);
	}
	
	public CompletableFuture<Suggestions> getSuggestions(LocalNBT nbt, List<String> path, String key, String value, int cursor, Collection<String> otherTags) {
		if (nbt instanceof LocalItem)
			return getSuggestions("item", nbt.getId(), nbt.getNBT(), path, key, value, cursor, otherTags);
		if (nbt instanceof LocalBlock)
			return getSuggestions("block", nbt.getId(), nbt.getNBT(), path, key, value, cursor, otherTags);
		if (nbt instanceof LocalEntity)
			return getSuggestions("entity", nbt.getId(), nbt.getNBT(), path, key, value, cursor, otherTags);
		return new SuggestionsBuilder("", 0).buildFuture();
	}
	public CompletableFuture<Suggestions> getSuggestions(LocalNBT nbt, List<String> path, String key, String value, int cursor) {
		return getSuggestions(nbt, path, key, value, cursor, null);
	}
	
}
