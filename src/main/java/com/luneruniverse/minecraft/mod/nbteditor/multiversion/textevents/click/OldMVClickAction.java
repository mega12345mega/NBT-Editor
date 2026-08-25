package com.luneruniverse.minecraft.mod.nbteditor.multiversion.textevents.click;

import java.lang.invoke.MethodType;
import java.util.Optional;
import java.util.function.Supplier;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;

import net.minecraft.text.ClickEvent;

public class OldMVClickAction implements MVClickAction<ClickEvent, String> {
	
	public static final OldMVClickAction OPEN_URL = new OldMVClickAction("open_url", ClickEvent.Action.OPEN_URL);
	public static final OldMVClickAction OPEN_FILE = new OldMVClickAction("open_file", ClickEvent.Action.OPEN_FILE);
	public static final OldMVClickAction RUN_COMMAND = new OldMVClickAction("run_command", ClickEvent.Action.RUN_COMMAND);
	public static final OldMVClickAction SUGGEST_COMMAND = new OldMVClickAction("suggest_command", ClickEvent.Action.SUGGEST_COMMAND);
	public static final OldMVClickAction CHANGE_PAGE = new OldMVClickAction("change_page", ClickEvent.Action.CHANGE_PAGE);
	public static final OldMVClickAction COPY_TO_CLIPBOARD = new OldMVClickAction("copy_to_clipboard", ClickEvent.Action.COPY_TO_CLIPBOARD);
	
	private final String name;
	private final ClickEvent.Action action;
	
	public OldMVClickAction(String name, ClickEvent.Action action) {
		this.name = name;
		this.action = action;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public Optional<String> parseValue(String valueStr) {
		return Optional.of(valueStr);
	}
	
	private static final Supplier<Reflection.MethodInvoker> ClickEvent_getValue =
			Reflection.getOptionalMethod(ClickEvent.class, "method_10844", MethodType.methodType(String.class));
	@Override
	public String getValue(ClickEvent event) {
		return ClickEvent_getValue.get().invoke(event);
	}
	@Override
	public String getStringifiedValue(ClickEvent event) {
		return getValue(event);
	}
	
	@Override
	public ClickEvent newEvent(String value) {
		return Reflection.newInstance(ClickEvent.class, new Class<?>[] {ClickEvent.Action.class, String.class}, action, value);
	}
	@Override
	public Optional<ClickEvent> newEventWithParse(String valueStr) {
		return Optional.of(newEvent(valueStr));
	}
	
}
