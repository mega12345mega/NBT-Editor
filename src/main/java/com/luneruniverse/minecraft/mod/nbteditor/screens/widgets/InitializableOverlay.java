package com.luneruniverse.minecraft.mod.nbteditor.screens.widgets;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionDrawable;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionElement;
import com.luneruniverse.minecraft.mod.nbteditor.screens.Tickable;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.screen.Screen;

public interface InitializableOverlay<T extends Screen> extends MultiVersionDrawable, MultiVersionElement, Tickable {
	public void init(T parent, int width, int height);
	public default void tick() {}
	
	@SuppressWarnings("unchecked")
	public default void initUnchecked(Screen parent) {
		init((T) parent, MainUtil.client.getWindow().getScaledWidth(), MainUtil.client.getWindow().getScaledHeight());
	}
}
