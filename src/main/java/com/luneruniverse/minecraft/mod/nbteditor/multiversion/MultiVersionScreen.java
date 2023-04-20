package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.lang.invoke.MethodType;
import java.util.function.Supplier;

import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class MultiVersionScreen extends Screen implements OldEventBehavior {
	
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
	
	private static final Supplier<Reflection.MethodInvoker> ParentElement_setInitialFocus =
			Reflection.getOptionalMethod(ParentElement.class, "method_20085", MethodType.methodType(void.class, Element.class));
	public void setInitialFocus(Element element) {
		switch (Version.get()) {
			case v1_19_4 -> {
				super.setInitialFocus(element);
				setFocused(element);
			}
			case v1_19_3, v1_19, v1_18 -> ParentElement_setInitialFocus.get().invoke(this, element);
		}
	}
	
}
