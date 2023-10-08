package com.luneruniverse.minecraft.mod.nbteditor.screens.configurable;

import net.minecraft.client.util.math.MatrixStack;

import java.util.function.BiFunction;

public class ConfigHiddenData<S extends ConfigPath, D> implements ConfigPath {
	
	protected final S visible;
	protected D data;
	protected final BiFunction<D, Boolean, D> onClone;
	
	public ConfigHiddenData(S visible, D data, BiFunction<D, Boolean, D> onClone) {
		this.visible = visible;
		this.data = data;
		this.onClone = onClone;
		visible.setParent(this);
	}
	
	public S getVisible() {
		return visible;
	}
	public void setData(D data) {
		this.data = data;
	}
	public D getData() {
		return data;
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		visible.render(matrices, mouseX, mouseY, delta);
	}
	
	@Override
	public boolean isValueValid() {
		return visible.isValueValid();
	}
	
	@Override
	public ConfigPath addValueListener(ConfigValueListener<ConfigValue<?, ?>> listener) {
		visible.addValueListener(listener);
		return this;
	}
	
	@Override
	public int getSpacingWidth() {
		return visible.getSpacingWidth();
	}
	
	@Override
	public int getRenderWidth() {
		return visible.getRenderWidth();
	}
	
	@Override
	public int getSpacingHeight() {
		return visible.getSpacingHeight();
	}
	
	@Override
	public int getRenderHeight() {
		return visible.getRenderHeight();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ConfigHiddenData<S, D> clone(boolean defaults) {
		return new ConfigHiddenData<>((S) visible.clone(defaults), onClone.apply(data, defaults), onClone);
	}
	
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		return visible.mouseClicked(mouseX, mouseY, button);
	}
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		return visible.mouseReleased(mouseX, mouseY, button);
	}
	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		visible.mouseMoved(mouseX, mouseY);
	}
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		return visible.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		return visible.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return visible.isMouseOver(mouseX, mouseY);
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return visible.keyPressed(keyCode, scanCode, modifiers);
	}
	@Override
	public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		return visible.keyReleased(keyCode, scanCode, modifiers);
	}
	@Override
	public boolean charTyped(char chr, int modifiers) {
		return visible.charTyped(chr, modifiers);
	}
	
	@Override
	public void tick() {
		visible.tick();
	}
	
}
