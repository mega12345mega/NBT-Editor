package com.luneruniverse.minecraft.mod.nbteditor.screens.configurable;

import java.util.ArrayList;
import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.screens.SimpleTooltip;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.ButtonWidget.TooltipSupplier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class ConfigItem<V extends ConfigValue<?, V>> implements ConfigPath {
	
	private final Text name;
	private final V value;
	private final int valueOffsetX;
	private final int valueOffsetY;
	
	private final List<ConfigValueListener<ConfigValue<?, ?>>> onChanged;
	
	private TooltipSupplier tooltip;
	
	public ConfigItem(Text name, V value) {
		this.name = name;
		this.value = value;
		this.valueOffsetX = MainUtil.client.textRenderer.getWidth(name) + PADDING;
		this.valueOffsetY = (getSpacingHeight() - value.getSpacingHeight()) / 2;
		
		this.onChanged = new ArrayList<>();
		value.addValueListener(source -> onChanged.forEach(listener -> listener.onValueChanged(source)));
		value.setParent(this);
	}
	private ConfigItem(Text name, V value, List<ConfigValueListener<ConfigValue<?, ?>>> onChanged) {
		this(name, value);
		this.onChanged.addAll(onChanged);
	}
	
	public V getValue() {
		return value;
	}
	
	public ConfigItem<V> setTooltip(TooltipSupplier tooltip) {
		this.tooltip = tooltip;
		return this;
	}
	public ConfigItem<V> setTooltip(String... keys) {
		setTooltip(new SimpleTooltip(keys));
		return this;
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		DrawableHelper.drawTextWithShadow(matrices, MainUtil.client.textRenderer, name, 0, (getSpacingHeight() - MainUtil.client.textRenderer.fontHeight) / 2, 0xFFFFFFFF);
		
		matrices.push();
		matrices.translate(valueOffsetX, valueOffsetY, 0);
		value.render(matrices, mouseX - valueOffsetX, mouseY - valueOffsetY, delta);
		matrices.pop();
		
		if (tooltip != null && mouseX >= 0 && mouseX <= valueOffsetX && isMouseOver(mouseX, mouseY))
			tooltip.onTooltip(null, matrices, mouseX, mouseY);
	}
	
	@Override
	public boolean isValueValid() {
		return value.isValueValid();
	}
	@Override
	public ConfigItem<V> addValueListener(ConfigValueListener<ConfigValue<?, ?>> listener) {
		onChanged.add(listener);
		return this;
	}
	
	@Override
	public int getSpacingWidth() {
		return valueOffsetX + value.getSpacingWidth();
	}
	
	@Override
	public int getSpacingHeight() {
		return Math.max(20, value.getSpacingHeight());
	}
	
	@Override
	public int getRenderWidth() {
		return valueOffsetX + value.getRenderWidth();
	}
	
	@Override
	public int getRenderHeight() {
		return Math.max(getSpacingHeight(), value.getRenderHeight());
	}
	
	@Override
	public ConfigItem<V> clone(boolean defaults) {
		ConfigItem<V> output = new ConfigItem<>(name, value.clone(defaults), onChanged);
		output.tooltip = tooltip;
		return output;
	}
	
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		return value.mouseClicked(mouseX - valueOffsetX, mouseY - valueOffsetY, button);
	}
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		return value.mouseReleased(mouseX - valueOffsetX, mouseY - valueOffsetY, button);
	}
	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		value.mouseMoved(mouseX - valueOffsetX, mouseY - valueOffsetY);
	}
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		return value.mouseDragged(mouseX - valueOffsetX, mouseY - valueOffsetY, button, deltaX, deltaY);
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		return value.mouseScrolled(mouseX - valueOffsetX, mouseY - valueOffsetY, amount);
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return value.keyPressed(keyCode, scanCode, modifiers);
	}
	@Override
	public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		return value.keyReleased(keyCode, scanCode, modifiers);
	}
	@Override
	public boolean charTyped(char chr, int modifiers) {
		return value.charTyped(chr, modifiers);
	}
	
}
