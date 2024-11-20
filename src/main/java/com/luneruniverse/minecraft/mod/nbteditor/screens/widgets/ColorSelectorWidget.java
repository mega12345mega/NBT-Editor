package com.luneruniverse.minecraft.mod.nbteditor.screens.widgets;

import java.awt.Color;
import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import com.luneruniverse.minecraft.mod.nbteditor.misc.Shaders;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IdentifierInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawable;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVElement;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVSliderWidget;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class ColorSelectorWidget extends GroupWidget {
	
	public static class ColorSelectorInput extends GroupWidget implements InputOverlay.Input<Integer> {
		
		private int color;
		
		public ColorSelectorInput(int color) {
			this.color = color;
		}
		
		@Override
		public void init(int x, int y) {
			clearWidgets();
			addWidget(new ColorSelectorWidget(x, y, 128, color, newColor -> color = newColor));
		}
		
		@Override
		public Integer getValue() {
			return color;
		}
		
		@Override
		public boolean isValid() {
			return true;
		}
		
		@Override
		public int getWidth() {
			return 128 + 4 + 64;
		}
		
		@Override
		public int getHeight() {
			return 128 + 24;
		}
		
	}
	
	private static final Identifier HUES = IdentifierInst.of("nbteditor", "textures/hues.png");
	
	private class ColorArea implements MVDrawable, MVElement {
		@Override
		public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
			MainUtil.fillShader(matrices, Shaders.POSITION_HSV, vertex -> MVMisc.setVertexLight(vertex, hueValue), x, y, areaSize, areaSize);
		}
		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (button != GLFW.GLFW_MOUSE_BUTTON_1 || !isMouseOver(mouseX, mouseY))
				return false;
			mouseDragged(mouseX, mouseY, button, 0, 0);
			return true;
		}
		@Override
		public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
			if (button != GLFW.GLFW_MOUSE_BUTTON_1)
				return false;
			mouseX = MathHelper.clamp(mouseX, x, x + areaSize);
			mouseY = MathHelper.clamp(mouseY, y, y + areaSize);
			color = Color.HSBtoRGB(hueValue / 360.0f, (float) (mouseX - x) / areaSize, 1 - (float) (mouseY - y) / areaSize);
			field.setText("#" + String.format("%08X", color).substring(2, 8)); // Calls onColor
			return true;
		}
		@Override
		public boolean isMouseOver(double mouseX, double mouseY) {
			return mouseX >= x && mouseX <= x + areaSize && mouseY >= y && mouseY <= y + areaSize;
		}
	}
	
	private final int x;
	private final int y;
	private final int areaSize;
	private final TextFieldWidget field;
	private int color;
	private int hueValue;
	
	public ColorSelectorWidget(int x, int y, int areaSize, int color, Consumer<Integer> onColor) {
		this.x = x;
		this.y = y;
		this.areaSize = areaSize;
		this.color = color;
		
		addWidget(new ColorArea());
		
		Color colorObj = new Color(color);
		hueValue = (int) (Color.RGBtoHSB(colorObj.getRed(), colorObj.getGreen(), colorObj.getBlue(), new float[3])[0] * 360);
		addWidget(new MVSliderWidget(x, y + areaSize + 4, areaSize, 20, hueValue / 359.0,
				() -> TextInst.translatable("nbteditor.color_selector.hue", hueValue), value -> hueValue = (int) (value * 359)) {
			@Override
			protected boolean renderSlider(MatrixStack matrices, int mouseX, int mouseY, float delta) {
				MVDrawableHelper.drawTexture(matrices, HUES, x + 4, y, 0, 0, width - 8, 20, width - 8, 20);
				return true;
			}
			@Override
			public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
				if (keyCode == GLFW.GLFW_KEY_RIGHT) {
					setValue(getValue() + 1 / 359.0);
					return true;
				}
				if (keyCode == GLFW.GLFW_KEY_LEFT) {
					setValue(getValue() - 1 / 359.0);
					return true;
				}
				return false;
			}
		});
		
		field = new TextFieldWidget(MainUtil.client.textRenderer, x + areaSize + 4, y + areaSize + 4, areaSize / 2, 20, TextInst.of(""));
		field.setMaxLength(7);
		field.setText("#" + String.format("%08X", color).substring(2, 8));
		field.setChangedListener(str -> {
			if (!str.matches("#[0-9a-fA-F]{6}"))
				return;
			this.color = Integer.parseInt(str.substring(1), 16);
			onColor.accept(this.color);
		});
		addWidget(field);
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);
		MVDrawableHelper.fill(matrices, x + areaSize + 4, y, x + areaSize + 4 + areaSize / 2, y + areaSize, color | 0xFF000000);
	}
	
}
