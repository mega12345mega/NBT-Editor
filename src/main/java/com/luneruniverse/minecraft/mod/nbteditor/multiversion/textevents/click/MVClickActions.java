package com.luneruniverse.minecraft.mod.nbteditor.multiversion.textevents.click;

import java.lang.invoke.MethodType;
import java.util.Optional;
import java.util.function.Supplier;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.textevents.MVTextEventAction;

import net.minecraft.text.ClickEvent;

public class MVClickActions {
	
	public static final MVClickAction<?, ?> OPEN_URL;
	public static final MVClickAction<?, String> OPEN_FILE;
	public static final MVClickAction<?, String> RUN_COMMAND;
	public static final MVClickAction<?, String> SUGGEST_COMMAND;
	public static final MVClickAction<?, ?> CHANGE_PAGE;
	public static final MVClickAction<?, String> COPY_TO_CLIPBOARD;
	
	static {
		if (MVTextEventAction.EVENTS_REFACTORED) {
			OPEN_URL = NewMVClickAction.OPEN_URL;
			OPEN_FILE = NewMVClickAction.OPEN_FILE;
			RUN_COMMAND = NewMVClickAction.RUN_COMMAND;
			SUGGEST_COMMAND = NewMVClickAction.SUGGEST_COMMAND;
			CHANGE_PAGE = NewMVClickAction.CHANGE_PAGE;
			COPY_TO_CLIPBOARD = NewMVClickAction.COPY_TO_CLIPBOARD;
		} else {
			OPEN_URL = OldMVClickAction.OPEN_URL;
			OPEN_FILE = OldMVClickAction.OPEN_FILE;
			RUN_COMMAND = OldMVClickAction.RUN_COMMAND;
			SUGGEST_COMMAND = OldMVClickAction.SUGGEST_COMMAND;
			CHANGE_PAGE = OldMVClickAction.CHANGE_PAGE;
			COPY_TO_CLIPBOARD = OldMVClickAction.COPY_TO_CLIPBOARD;
		}
	}
	
	public static final MVClickAction<?, ?>[] VALUES = new MVClickAction<?, ?>[] {OPEN_URL, OPEN_FILE, RUN_COMMAND, SUGGEST_COMMAND, CHANGE_PAGE, COPY_TO_CLIPBOARD};
	
	public static MVClickAction<?, ?> fromName(String name) {
		for (MVClickAction<?, ?> action : VALUES) {
			if (action.getName().equals(name))
				return action;
		}
		throw new IllegalArgumentException("Invalid MVClickAction name: " + name);
	}
	
	private static final Supplier<Reflection.MethodInvoker> ClickEvent_getAction =
			Reflection.getOptionalMethod(ClickEvent.class, "method_10845", MethodType.methodType(ClickEvent.Action.class));
	@SuppressWarnings("unchecked")
	public static <E extends ClickEvent> MVClickAction<? extends E, ?> getAction(E event) {
		return (MVClickAction<? extends E, ?>) switch (Version.<ClickEvent.Action>newSwitch()
				.range("1.21.5", null, () -> event.getAction())
				.range(null, "1.21.4", () -> ClickEvent_getAction.get().invoke(event))
				.get()) {
			case OPEN_URL -> OPEN_URL;
			case OPEN_FILE -> OPEN_FILE;
			case RUN_COMMAND -> RUN_COMMAND;
			case SUGGEST_COMMAND -> SUGGEST_COMMAND;
			case CHANGE_PAGE -> CHANGE_PAGE;
			case COPY_TO_CLIPBOARD -> COPY_TO_CLIPBOARD;
		};
	}
	
	@SuppressWarnings("unchecked")
	public static Object getValue(ClickEvent event) {
		return ((MVClickAction<ClickEvent, ?>) getAction(event)).getValue(event);
	}
	@SuppressWarnings("unchecked")
	public static <V> Optional<V> getValue(MVClickAction<?, V> action, ClickEvent event) {
		MVClickAction<?, ?> actualAction = getAction(event);
		if (action != actualAction)
			return Optional.empty();
		return Optional.of(((MVClickAction<ClickEvent, V>) actualAction).getValue(event));
	}
	
	@SuppressWarnings("unchecked")
	public static String getStringifiedValue(ClickEvent event) {
		return ((MVClickAction<ClickEvent, ?>) getAction(event)).getStringifiedValue(event);
	}
	
}
