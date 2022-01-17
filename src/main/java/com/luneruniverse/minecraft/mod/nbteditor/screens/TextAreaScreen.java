package com.luneruniverse.minecraft.mod.nbteditor.screens;

import java.util.function.Consumer;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

@Deprecated
/**
 * Class unfinished, doesn't work
 */
public class TextAreaScreen extends Screen {
	
	private final Screen parent;
	private String text;
	private final Consumer<String> onDone;
	
	public TextAreaScreen(Screen parent, String text, Consumer<String> onDone) {
		super(Text.of("Text Area"));
		this.parent = parent;
		this.text = text;
		this.onDone = onDone;
	}
	
	@Override
	protected void init() {
		this.addDrawableChild(new ButtonWidget(20, 20, width / 2 - 25, 20, ScreenTexts.DONE, btn -> {
			onDone.accept(text);
			onClose();
		}));
		this.addDrawableChild(new ButtonWidget(width / 2 + 5, 20, width / 2 - 25, 20, ScreenTexts.CANCEL, btn -> onClose()));
		
		// TODO
	}
	
	@Override
	public void onClose() {
		this.client.setScreen(parent);
	}
	
}
