package com.luneruniverse.minecraft.mod.nbteditor.screens.widgets;

import java.util.function.Consumer;
import java.util.function.Predicate;

import org.lwjgl.glfw.GLFW;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;

public class StringInputWidget extends GroupWidget implements InitializableOverlay<Screen> {
	
	private final String initialValue;
	private final Consumer<String> valueConsumer;
	private final Predicate<String> valueValidator;
	private final Runnable close;
	private TextFieldWidget value;
	private ButtonWidget ok;
	
	public StringInputWidget(String initialValue, Consumer<String> valueConsumer, Predicate<String> valueValidator, Consumer<StringInputWidget> close) {
		this.initialValue = initialValue;
		this.valueConsumer = valueConsumer;
		this.valueValidator = valueValidator;
		this.close = () -> close.accept(this);
	}
	
	@Override
	public void init(Screen parent, int width, int height) {
		clearWidgets();
		
		String prevValue = (value == null ? initialValue : value.getText());
		value = new TextFieldWidget(MainUtil.client.textRenderer, width / 2 - 104, height / 2 - 20, 208, 16, TextInst.of(""));
		value.setMaxLength(Integer.MAX_VALUE);
		value.setText(prevValue);
		addWidget(value);
		setFocused(value);
		
		ok = addWidget(MVMisc.newButton(width / 2 - 104, height / 2 + 4, 100, 20, TextInst.translatable("nbteditor.ok"), btn -> {
			this.close.run();
			valueConsumer.accept(value.getText());
		}));
		addWidget(MVMisc.newButton(width / 2 + 4, height / 2 + 4, 100, 20, TextInst.translatable("nbteditor.cancel"), btn -> {
			this.close.run();
		}));
		
		value.setChangedListener(str -> ok.active = valueValidator.test(str));
		ok.active = valueValidator.test(value.getText());
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		matrices.push();
		matrices.translate(0.0, 0.0, 500.0);
		MainUtil.client.currentScreen.renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		MainUtil.renderLogo(matrices);
		matrices.pop();
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			close.run();
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_ENTER && ok.active) {
			close.run();
			valueConsumer.accept(value.getText());
			return true;
		}
		
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
}
