package com.luneruniverse.minecraft.mod.nbteditor.fancytext;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;

import net.minecraft.text.ClickEvent;

public enum ClickAction implements TextAction {
	NONE(null),
	OPEN_URL(ClickEvent.Action.OPEN_URL),
	RUN_COMMAND(ClickEvent.Action.RUN_COMMAND),
	SUGGEST_COMMAND(ClickEvent.Action.SUGGEST_COMMAND),
	CHANGE_PAGE(ClickEvent.Action.CHANGE_PAGE),
	COPY_TO_CLIPBOARD(ClickEvent.Action.COPY_TO_CLIPBOARD);
	
	public static ClickAction get(ClickEvent.Action value) {
		for (ClickAction action : values()) {
			if (action.value == value)
				return action;
		}
		if (value == ClickEvent.Action.OPEN_FILE)
			return NONE;
		throw new IllegalArgumentException("Invalid click action: " + value);
	}
	
	private final ClickEvent.Action value;
	
	private ClickAction(ClickEvent.Action value) {
		this.value = value;
	}
	
	public ClickEvent toEvent(String value) {
		if (this == NONE)
			return null;
		return new ClickEvent(this.value, value);
	}
	
	@Override
	public String toString() {
		if (this == NONE)
			return "none";
		return MVMisc.getClickEventActionName(value);
	}
}
