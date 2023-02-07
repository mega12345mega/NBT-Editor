package com.luneruniverse.minecraft.mod.nbteditor.screens;

import java.util.function.Consumer;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionScreen;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.ScreenTexts;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.util.NbtFormatter;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;

public class TextAreaScreen extends MultiVersionScreen {
	
	private final Screen parent;
	private String text;
	private final NbtFormatter.Impl formatter;
	private final Consumer<String> onDone;
	
	private MultiLineTextFieldWidget textArea;
	
	public TextAreaScreen(Screen parent, String text, NbtFormatter.Impl formatter, Consumer<String> onDone) {
		super(TextInst.of("Text Area"));
		this.parent = parent;
		this.text = text;
		this.formatter = formatter;
		this.onDone = onDone;
	}
	public TextAreaScreen(Screen parent, String text, Consumer<String> onDone) {
		this(parent, text, null, onDone);
	}
	
	@Override
	protected void init() {
		MultiVersionMisc.setKeyboardRepeatEvents(true);
		
		ButtonWidget done;
		this.addDrawableChild(done = MultiVersionMisc.newButton(20, 20, Math.min(200, width / 2 - 25), 20, ScreenTexts.DONE, btn -> {
			onDone.accept(text);
			close();
		}));
		if (width - (done.getWidth() * 2 + 50) < 100) // When the end of the second button is near the end of the text field, it looks bad
			done.setWidth(done.getWidth() * 2 / 3);
		this.addDrawableChild(MultiVersionMisc.newButton(done.x + done.getWidth() + 10, 20, done.getWidth(), 20, ScreenTexts.CANCEL, btn -> close()));
		
		this.addDrawableChild(textArea = new MultiLineTextFieldWidget(20, 50, width - 40, height - 70, text, formatter == null ? null : str -> {
			NbtFormatter.FormatterResult formattedText = formatter.formatSafely(str);
			done.active = formattedText.isSuccess();
			return formattedText.text();
		}, newText -> text = newText));
		setInitialFocus(textArea);
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
	}
	
	@Override
	public void tick() {
		if (textArea != null)
			textArea.tick();
	}
	
	@Override
	public void close() {
		this.client.setScreen(parent);
	}
	
	@Override
	public void removed() {
		MultiVersionMisc.setKeyboardRepeatEvents(false);
	}
	
}
