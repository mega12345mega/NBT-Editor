package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.lang.invoke.MethodType;
import java.util.function.Supplier;

import net.minecraft.client.gui.Element;

public interface MVElementParent {
	static final Supplier<Reflection.MethodInvoker> Element_mouseScrolled =
			Reflection.getOptionalMethod(Element.class, "method_25401", MethodType.methodType(boolean.class, double.class, double.class, double.class));
	public default boolean mouseScrolled(double mouseX, double mouseY, double xAmount, double yAmount) {
		return Element_mouseScrolled.get().invoke(this, mouseX, mouseY, yAmount);
	}
}
