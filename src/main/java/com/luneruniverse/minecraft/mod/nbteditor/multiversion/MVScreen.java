package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.lang.invoke.MethodType;
import java.util.function.Supplier;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class MVScreen extends Screen implements OldEventBehavior {
	
	protected MVScreen(Text title) {
		super(title);
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		MVDrawableHelper.super_render(MVScreen.class, this, matrices, mouseX, mouseY, delta);
	}
	public final void method_25394(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		render(matrices, mouseX, mouseY, delta);
	}
	@Override
	public final void render(DrawContext context, int mouseX, int mouseY, float delta) {
		render(MVDrawableHelper.getMatrices(context), mouseX, mouseY, delta);
	}
	
	// Needed for some reason ...
	// Copied from MultiVersionScreenParent
	// Prevents crash in 1.17 that's trying to find this method
	@Override
	public void renderBackground(MatrixStack matrices) {
		MVDrawableHelper.renderBackground((Screen) this, matrices);
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
	
	private static final Supplier<Reflection.MethodInvoker> ParentElement_setInitialFocus =
			Reflection.getOptionalMethod(ParentElement.class, "method_20085", MethodType.methodType(void.class, Element.class));
	public void setInitialFocus(Element element) {
		Version.newSwitch()
				.range("1.19.4", null, () -> {
					super.setInitialFocus(element);
					setFocused(element);
				})
				.range(null, "1.19.3", () -> ParentElement_setInitialFocus.get().invoke(this, element))
				.run();
	}
	
	@Override
	protected void setInitialFocus() {}
	
}
