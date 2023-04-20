package com.luneruniverse.minecraft.mod.nbteditor.misc;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.luneruniverse.minecraft.mod.nbteditor.screens.CreativeTab;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;

// Non-mixin classes in the mixin package doesn't work well
public class MixinLink {
	
	public static void addCreativeTabs(Screen source) {
		int i = -1;
		List<CreativeTab> tabs = new ArrayList<>();
		for (CreativeTab.CreativeTabData tab : CreativeTab.TABS) {
			if (tab.whenToShow().test(source))
				tabs.add(new CreativeTab(source, (++i) * (CreativeTab.WIDTH + 2) + 10, tab.item(), tab.onClick()));
		}
		if (!tabs.isEmpty())
			source.addDrawableChild(new CreativeTab.CreativeTabGroup(tabs));
	}
	
	
	private static final Map<String, Runnable> events = new HashMap<>();
	public static Style withRunClickEvent(Style style, Runnable onClick) {
		String id = "\0nbteditor_runnable@" + new Random().nextLong(); // \0 is not valid in file paths on most OSs
		events.put(id, onClick);
		return style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, id));
	}
	public static boolean tryRunClickEvent(String id) {
		Runnable onClick = events.get(id);
		if (onClick != null) {
			onClick.run();
			return true;
		}
		return false;
	}
	
	
	public static File screenshotTarget;
	
}
