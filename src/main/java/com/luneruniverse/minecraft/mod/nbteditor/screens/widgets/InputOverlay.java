package com.luneruniverse.minecraft.mod.nbteditor.screens.widgets;

import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;

public class InputOverlay<T> extends GroupWidget implements InitializableOverlay<Screen> {
	
	public static interface Input<T> extends Drawable, Element {
		public void init(int x, int y);
		public int getWidth();
		public int getHeight();
		public T getValue();
		public boolean isValid();
	}
	
	private final Input<T> input;
	private final Consumer<T> valueConsumer;
	private final Runnable close;
	private ButtonWidget ok;
	
	public InputOverlay(Input<T> input, Consumer<T> valueConsumer, Runnable close) {
		this.input = input;
		this.valueConsumer = valueConsumer;
		this.close = close;
	}
	
	@Override
	public void init(Screen parent, int width, int height) {
		clearWidgets();
		
		int x = (width - input.getWidth()) / 2;
		int y = (height - input.getHeight() - 24) / 2;
		input.init(x, y);
		addWidget(input);
		
		ok = addWidget(MVMisc.newButton(x, y + input.getHeight() + 4,
				(input.getWidth() - 4) / 2, 20, TextInst.translatable("nbteditor.ok"), btn -> {
			this.close.run();
			valueConsumer.accept(input.getValue());
		}));
		addWidget(MVMisc.newButton(width / 2 + 2, y + input.getHeight() + 4,
				(input.getWidth() - 4) / 2, 20, TextInst.translatable("nbteditor.cancel"), btn -> {
			this.close.run();
		}));
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		ok.active = input.isValid();
		
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
			valueConsumer.accept(input.getValue());
			return true;
		}
		
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
}
