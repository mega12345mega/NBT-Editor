package com.luneruniverse.minecraft.mod.nbteditor.fancytext;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;

import net.minecraft.text.HoverEvent;

public enum HoverAction implements TextAction {
	NONE(null),
	SHOW_TEXT(HoverEvent.Action.SHOW_TEXT),
	SHOW_ITEM(HoverEvent.Action.SHOW_ITEM),
	SHOW_ENTITY(HoverEvent.Action.SHOW_ENTITY);
	
	public static HoverAction get(HoverEvent.Action<?> value) {
		for (HoverAction action : values()) {
			if (action.value == value)
				return action;
		}
		throw new IllegalArgumentException("Invalid hover action: " + value);
	}
	
	private final HoverEvent.Action<?> value;
	
	private HoverAction(HoverEvent.Action<?> value) {
		this.value = value;
	}
	
	public HoverEvent toEvent(String value) {
		if (this == NONE)
			return null;
		JsonObject json = new JsonObject();
		json.addProperty("action", MVMisc.getHoverEventActionName(this.value));
		json.add("contents", new Gson().fromJson(value, JsonElement.class));
		return MVMisc.getHoverEvent(json);
	}
	
	@Override
	public String toString() {
		if (this == NONE)
			return "none";
		return MVMisc.getHoverEventActionName(value);
	}
}
