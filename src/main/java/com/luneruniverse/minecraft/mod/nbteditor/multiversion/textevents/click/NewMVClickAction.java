package com.luneruniverse.minecraft.mod.nbteditor.multiversion.textevents.click;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.function.Function;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;

import net.minecraft.text.ClickEvent;
import net.minecraft.util.Util;

public class NewMVClickAction<E extends ClickEvent, V> implements MVClickAction<E, V> {
	
	private static final Function<String, Optional<URI>> parseUri = valueStr -> {
		try {
			return Optional.of(Util.validateUri(valueStr));
		} catch (URISyntaxException e) {
			return Optional.empty();
		}
	};
	private static final Function<String, Optional<String>> parseStr = Optional::of;
	private static final Function<String, Optional<String>> parseCmd = valueStr -> {
		return valueStr.chars().allMatch(c -> MVMisc.isValidChar((char) c)) ? Optional.of(valueStr) : Optional.empty();
	};
	private static final Function<String, Optional<Integer>> parsePage = valueStr -> {
		try {
			int page = Integer.parseInt(valueStr);
			if (page >= 1)
				return Optional.of(page);
		} catch (NumberFormatException e) {}
		return Optional.empty();
	};
	
	public static final NewMVClickAction<ClickEvent.OpenUrl, URI> OPEN_URL = new NewMVClickAction<>("open_url", parseUri, ClickEvent.OpenUrl::uri, ClickEvent.OpenUrl::new);
	public static final NewMVClickAction<ClickEvent.OpenFile, String> OPEN_FILE = new NewMVClickAction<>("open_file", parseStr, ClickEvent.OpenFile::path, ClickEvent.OpenFile::new);
	public static final NewMVClickAction<ClickEvent.RunCommand, String> RUN_COMMAND = new NewMVClickAction<>("run_command", parseCmd, ClickEvent.RunCommand::command, ClickEvent.RunCommand::new);
	public static final NewMVClickAction<ClickEvent.SuggestCommand, String> SUGGEST_COMMAND = new NewMVClickAction<>("suggest_command", parseCmd, ClickEvent.SuggestCommand::command, ClickEvent.SuggestCommand::new);
	public static final NewMVClickAction<ClickEvent.ChangePage, Integer> CHANGE_PAGE = new NewMVClickAction<>("change_page", parsePage, ClickEvent.ChangePage::page, ClickEvent.ChangePage::new);
	public static final NewMVClickAction<ClickEvent.CopyToClipboard, String> COPY_TO_CLIPBOARD = new NewMVClickAction<>("copy_to_clipboard", parseStr, ClickEvent.CopyToClipboard::value, ClickEvent.CopyToClipboard::new);
	
	private final String name;
	private final Function<String, Optional<V>> parser;
	private final Function<E, V> getter;
	private final Function<V, E> constructor;
	
	public NewMVClickAction(String name, Function<String, Optional<V>> parser, Function<E, V> getter, Function<V, E> constructor) {
		this.name = name;
		this.parser = parser;
		this.getter = getter;
		this.constructor = constructor;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public Optional<V> parseValue(String valueStr) {
		return parser.apply(valueStr);
	}
	
	@Override
	public V getValue(E event) {
		return getter.apply(event);
	}
	@Override
	public String getStringifiedValue(E event) {
		return getter.apply(event).toString();
	}
	
	@Override
	public E newEvent(V value) {
		return constructor.apply(value);
	}
	@Override
	public Optional<E> newEventWithParse(String valueStr) {
		return parser.apply(valueStr).map(constructor);
	}
	
}
