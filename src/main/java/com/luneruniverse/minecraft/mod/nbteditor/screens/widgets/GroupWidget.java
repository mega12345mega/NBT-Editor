package com.luneruniverse.minecraft.mod.nbteditor.screens.widgets;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawable;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVElement;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.OldEventBehavior;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.screens.Tickable;

import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.util.math.MatrixStack;

public class GroupWidget extends AbstractParentElement implements MVDrawable, MVElement, Tickable, Selectable, OldEventBehavior {
	
	private final List<Drawable> drawables;
	private final List<Element> elements;
	private final List<Tickable> tickables;
	
	public GroupWidget() {
		this.drawables = new ArrayList<>();
		this.elements = new ArrayList<>();
		this.tickables = new ArrayList<>();
	}
	
	public <T extends Drawable> T addDrawable(T drawable) {
		if (!this.drawables.contains(drawable))
			this.drawables.add(drawable);
		return drawable;
	}
	
	public <T extends Element> T addElement(T element) {
		if (!this.elements.contains(element))
			this.elements.add(element);
		return element;
	}
	
	public <T extends Tickable> T addTickable(T tickable) {
		if (!this.tickables.contains(tickable))
			this.tickables.add(tickable);
		return tickable;
	}
	
	public <T extends Drawable & Element> T addWidget(T widget) {
		addDrawable(widget);
		addElement(widget);
		if (widget instanceof Tickable tickable)
			addTickable(tickable);
		return widget;
	}
	
	public boolean removeDrawable(Drawable drawable) {
		return this.drawables.remove(drawable);
	}
	
	public boolean removeElement(Element element) {
		return this.elements.remove(element);
	}
	
	public boolean removeTickable(Tickable tickable) {
		return this.tickables.remove(tickable);
	}
	
	public <T extends Drawable & Element> boolean removeWidget(T widget) {
		return removeDrawable(widget) | removeElement(widget) |
				(widget instanceof Tickable tickable && removeTickable(tickable));
	}
	
	public boolean filterDrawables(Predicate<Drawable> filter) {
		return this.drawables.removeIf(filter.negate());
	}
	
	public boolean filterElements(Predicate<Element> filter) {
		return this.elements.removeIf(filter.negate());
	}
	
	public boolean filterTickables(Predicate<Tickable> filter) {
		return this.tickables.removeIf(filter.negate());
	}
	
	public boolean filterWidgets(Predicate<Object> filter) {
		Set<Object> widgets = new HashSet<>();
		widgets.addAll(drawables);
		widgets.addAll(elements);
		widgets.addAll(tickables);
		widgets.removeIf(filter.negate());
		boolean output = drawables.retainAll(widgets);
		output |= elements.retainAll(widgets);
		return output | tickables.retainAll(widgets);
	}
	
	public boolean clearDrawables() {
		if (drawables.isEmpty())
			return false;
		drawables.clear();
		return true;
	}
	
	public boolean clearElements() {
		if (elements.isEmpty())
			return false;
		elements.clear();
		return true;
	}
	
	public boolean clearTickables() {
		if (tickables.isEmpty())
			return false;
		tickables.clear();
		return true;
	}
	
	public boolean clearWidgets() {
		return clearDrawables() | clearElements() | clearTickables();
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		for (Drawable drawable : drawables)
			drawable.render(matrices, mouseX, mouseY, delta);
	}
	
	@Override
	public void tick() {
		for (Tickable tickable : tickables)
			tickable.tick();
	}
	
	@Override
	public List<? extends Element> children() {
		return new ArrayList<>(elements);
	}
	
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		for (Element element : elements) {
			if (element.isMouseOver(mouseX, mouseY))
				return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void setFocused(boolean focused) {
		MVElement.super.setFocused(focused);
	}
	@SuppressWarnings("deprecation")
	@Override
	public boolean isFocused() {
		return MVElement.super.isFocused();
	}
	public boolean method_25401(double mouseX, double mouseY, double amount) {
		return Version.<Boolean>newSwitch()
				.range("1.20.2", null, () -> MVElement.super.method_25401(mouseX, mouseY, amount))
				.range(null, "1.20.1", () -> super_mouseScrolled(mouseX, mouseY, amount))
				.get();
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double xAmount, double yAmount) {
		return super.mouseScrolled(mouseX, mouseY, xAmount, yAmount);
	}
	private boolean super_mouseScrolled(double mouseX, double mouseY, double amount) {
		try {
			return (boolean) MethodHandles.privateLookupIn(GroupWidget.class, MethodHandles.lookup())
					.findSpecial(AbstractParentElement.class, "method_25401",
					MethodType.methodType(boolean.class, double.class, double.class, double.class),
					GroupWidget.class).invoke(this, mouseX, mouseY, amount);
		} catch (Throwable e) {
			throw new RuntimeException("Error calling super.mouseScrolled", e);
		}
	}
	
	
	
	@Override
	public SelectionType getType() {
		return SelectionType.NONE;
	}
	
	@Override
	public void appendNarrations(NarrationMessageBuilder var1) {
		
	}
	
}
