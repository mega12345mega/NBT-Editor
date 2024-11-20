package com.luneruniverse.minecraft.mod.nbteditor.screens.util;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.ScreenTexts;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.screens.OverlaySupportingScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.MultiLineTextFieldWidget;
import com.luneruniverse.minecraft.mod.nbteditor.util.NbtFormatter;
import com.mojang.brigadier.suggestion.Suggestions;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;

public class TextAreaScreen extends OverlaySupportingScreen {
	
	private final Screen parent;
	private String text;
	private final NbtFormatter.Impl formatter;
	private final boolean newLines;
	private final Consumer<String> onDone;
	
	private MultiLineTextFieldWidget textArea;
	private BiFunction<String, Integer, CompletableFuture<Suggestions>> suggestions;
	
	public TextAreaScreen(Screen parent, String text, NbtFormatter.Impl formatter, boolean newLines, Consumer<String> onDone) {
		super(TextInst.of("Text Area"));
		this.parent = parent;
		this.text = text;
		this.formatter = formatter;
		this.newLines = newLines;
		this.onDone = onDone;
	}
	public TextAreaScreen(Screen parent, String text, boolean newLines, Consumer<String> onDone) {
		this(parent, text, null, newLines, onDone);
	}
	
	public TextAreaScreen suggest(BiFunction<String, Integer, CompletableFuture<Suggestions>> suggestions) {
		this.suggestions = suggestions;
		if (textArea != null)
			textArea.suggest(this, suggestions);
		return this;
	}
	
	@Override
	protected void init() {
		super.init();
		MVMisc.setKeyboardRepeatEvents(true);
		
		ButtonWidget done;
		this.addDrawableChild(done = MVMisc.newButton(20, 20, Math.min(200, width / 2 - 25), 20, ScreenTexts.DONE, btn -> {
			onDone.accept(text);
			close();
		}));
		if (width - (done.getWidth() * 2 + 50) < 100) // When the end of the second button is near the end of the text field, it looks bad
			done.setWidth(done.getWidth() * 2 / 3);
		this.addDrawableChild(MVMisc.newButton(done.x + done.getWidth() + 10, 20, done.getWidth(), 20, ScreenTexts.CANCEL, btn -> close()));
		
		textArea = addDrawableChild(MultiLineTextFieldWidget.create(textArea, 20, 50, width - 40, height - 70, text, formatter == null ? null : str -> {
			NbtFormatter.FormatterResult formattedText = formatter.formatSafely(str);
			done.active = formattedText.isSuccess();
			return formattedText.text();
		}, newLines, newText -> text = newText));
		if (suggestions != null)
			textArea.suggest(this, suggestions);
		setInitialFocus(textArea);
	}
	
	@Override
	public void renderMain(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.renderBackground(matrices);
		super.renderMain(matrices, mouseX, mouseY, delta);
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (getOverlay() == null && textArea.keyPressed(keyCode, scanCode, modifiers))
			return true;
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}
	
	@Override
	public void close() {
		this.client.setScreen(parent);
	}
	
	@Override
	public void removed() {
		MVMisc.setKeyboardRepeatEvents(false);
	}
	
}
