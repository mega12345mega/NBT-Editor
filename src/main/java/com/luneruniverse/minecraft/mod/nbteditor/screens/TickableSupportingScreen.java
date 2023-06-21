package com.luneruniverse.minecraft.mod.nbteditor.screens;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionScreen;

import net.minecraft.client.gui.Element;
import net.minecraft.text.Text;

public class TickableSupportingScreen extends MultiVersionScreen {
	
	protected TickableSupportingScreen(Text title) {
		super(title);
	}
	
	@Override
	public void tick() {
		for (Element element : children()) {
			if (element instanceof Tickable tickable)
				tickable.tick();
		}
	}
	
}
