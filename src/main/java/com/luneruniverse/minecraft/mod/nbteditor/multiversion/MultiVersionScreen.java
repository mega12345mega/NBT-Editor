package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class MultiVersionScreen extends Screen {
	
	protected MultiVersionScreen(Text title) {
		super(title);
	}
	
	public final boolean isPauseScreen() { // 1.18
		return shouldPause();
	}
	public boolean shouldPause() { // 1.19
		return true;
	}
	
	public final void onClose() { // 1.18
		close();
	}
	public void close() { // 1.19
		client.setScreen(null);
	}
	
}
