package com.luneruniverse.minecraft.mod.nbteditor.screens;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class TextAreaScreen extends Screen {
	
	private final Screen parent;
	private String text;
	private final Function<String, Map.Entry<Boolean, Text>> formatter;
	private final Consumer<String> onDone;
	
	private MultiLineTextFieldWidget textArea;
	
	public TextAreaScreen(Screen parent, String text, Function<String, Map.Entry<Boolean, Text>> formatter, Consumer<String> onDone) {
		super(Text.of("Text Area"));
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
		client.keyboard.setRepeatEvents(true);
		
		ButtonWidget done;
		this.addDrawableChild(done = new ButtonWidget(20, 20, Math.min(200, width / 2 - 25), 20, ScreenTexts.DONE, btn -> {
			onDone.accept(text);
			close();
		}));
		if (width - (done.getWidth() * 2 + 50) < 100) // When the end of the second button is near the end of the text field, it looks bad
			done.setWidth(done.getWidth() * 2 / 3);
		this.addDrawableChild(new ButtonWidget(done.x + done.getWidth() + 10, 20, done.getWidth(), 20, ScreenTexts.CANCEL, btn -> close()));
		
		this.addDrawableChild(textArea = new MultiLineTextFieldWidget(20, 50, width - 40, height - 70, text, formatter == null ? null : str -> {
			Map.Entry<Boolean, Text> formattedText = formatter.apply(str);
			done.active = formattedText.getKey();
			return formattedText.getValue();
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
		client.keyboard.setRepeatEvents(false);
	}
	
}
