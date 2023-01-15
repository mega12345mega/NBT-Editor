package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class ExtendableButtonWidget extends PressableWidget {
	
	@FunctionalInterface
	public interface PressAction {
		public void onPress(ExtendableButtonWidget button);
	}
	
	private final PressAction onPress;
	private final MultiVersionTooltip tooltip;
	
	public ExtendableButtonWidget(int x, int y, int width, int height, Text text, PressAction onPress, MultiVersionTooltip tooltip) {
		super(x, y, width, height, text);
		this.onPress = onPress;
		this.tooltip = tooltip;
		if (tooltip != null) {
			switch (Version.get()) {
				case v1_19_3 -> setTooltip(tooltip.toNewTooltip());
				case v1_19, v1_18 -> {}
			}
		}
	}
	public ExtendableButtonWidget(int x, int y, int width, int height, Text text, PressAction onPress) {
		this(x, y, width, height, text, onPress, null);
	}
	
	@Override
	public void onPress() {
		onPress.onPress(this);
	}
	
	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
		appendDefaultNarrations(builder);
	}
	
	@Override
	public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.renderButton(matrices, mouseX, mouseY, delta);
		switch (Version.get()) {
			case v1_19_3 -> {}
			case v1_19, v1_18 -> {
				if (isHovered())
					method_25352(matrices, mouseX, mouseY);
			}
		}
	}
	public void method_25352(MatrixStack matrices, int mouseX, int mouseY) { // renderTooltip
		if (tooltip != null)
			tooltip.render(matrices, mouseX, mouseY);
	}
	
}
