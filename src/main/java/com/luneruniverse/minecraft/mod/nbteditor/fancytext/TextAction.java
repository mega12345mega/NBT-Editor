package com.luneruniverse.minecraft.mod.nbteditor.fancytext;

import java.util.Arrays;
import java.util.stream.Stream;

public interface TextAction {
	
	public static final TextAction[] VALUES = Stream.concat(
			Arrays.stream(ClickAction.values()), Arrays.stream(HoverAction.values())).toArray(TextAction[]::new);
	
	public static TextAction valueOf(String name) {
		if (name.equals("NONE"))
			return null;
		for (TextAction action : VALUES) {
			if (action.name().equals(name))
				return action;
		}
		return null;
	}
	
	public String name();
	
}
