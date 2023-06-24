package com.luneruniverse.minecraft.mod.nbteditor.screens.configurable;

import java.util.ArrayList;
import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.ExtendableButtonWidget;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;

public class ConfigValueDropdown<T> extends ExtendableButtonWidget implements ConfigValue<T, ConfigValueDropdown<T>> {
	
	protected T value;
	protected boolean open;
	protected final T defaultValue;
	protected final List<T> allValues;
	protected final List<T> importantValues;
	
	protected final List<ConfigValueListener<ConfigValueDropdown<T>>> onChanged;
	
	@SuppressWarnings("unchecked")
	public ConfigValueDropdown(T value, T defaultValue, List<T> allValues, List<T> importantValues) {
		super(0, 0, getMaxWidth(allValues) + MainUtil.client.textRenderer.fontHeight * 2, 20, TextInst.of(value.toString()),
				btn -> ((ConfigValueDropdown<T>) btn).open = !((ConfigValueDropdown<T>) btn).open);
		
		this.value = value;
		this.defaultValue = defaultValue;
		this.allValues = allValues;
		this.importantValues = importantValues;
		this.open = false;
		this.onChanged = new ArrayList<>();
	}
	public ConfigValueDropdown(T value, T defaultValue, List<T> allValues) {
		this(value, defaultValue, allValues, new ArrayList<>());
	}
	private static int getMaxWidth(List<?> allValues) {
		return allValues.stream().map(Object::toString).mapToInt(MainUtil.client.textRenderer::getWidth).max().orElse(0);
	}
	private ConfigValueDropdown(T value, T defaultValue, List<T> allValues, List<T> importantValues, boolean open, List<ConfigValueListener<ConfigValueDropdown<T>>> onChanged) {
		this(value, defaultValue, allValues, importantValues);
		this.open = open;
		this.onChanged.addAll(onChanged);
	}
	
	@Override
	public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.renderButton(matrices, mouseX, mouseY, delta);
		if (open) {
			matrices.push();
			matrices.translate(0.0, 0.0, 401.0);
			
			MVDrawableHelper.fill(matrices, this.x, this.height, this.x + this.width, allValues.size() * this.height, 0xFF000000);
			boolean xHover = this.active && mouseX >= this.x && mouseX < this.x + this.width;
			int i = 0;
			for (T option : allValues) {
				if (option.equals(value))
					continue;
				int y = this.y + (++i * this.height);
				int color = -1;
				if (xHover && mouseY >= y && mouseY < y + this.height)
					color = 0xFF257789;
				else if (importantValues.contains(option))
					color = 0xFFFFAA00;
				MVDrawableHelper.drawCenteredTextWithShadow(matrices, MainUtil.client.textRenderer, TextInst.of(option.toString()),
						this.x + this.width / 2, y + (this.height - MainUtil.client.textRenderer.fontHeight) / 2, color);
				if (color != -1 && option instanceof ConfigTooltipSupplier) // Hovering
					((ConfigTooltipSupplier) option).getTooltip().render(matrices, mouseX, mouseY);
			}
			
			matrices.pop();
		}
		if (isSelected() && value instanceof ConfigTooltipSupplier)
			((ConfigTooltipSupplier) value).getTooltip().render(matrices, mouseX, mouseY);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		boolean output = super.mouseClicked(mouseX, mouseY, button);
		if (!output && this.active && this.visible && open && mouseX >= this.x && mouseX < this.x + this.width) {
			int i = 0;
			for (T option : allValues) {
				if (option.equals(value))
					continue;
				int y = this.y + (++i * this.height);
				if (mouseY >= y && mouseY < y + this.height) {
					this.playDownSound(MinecraftClient.getInstance().getSoundManager());
					setValue(option);
					open = false;
					return true;
				}
			}
		}
		if (!output && open)
			open = false;
		return output;
	}
	
	public void setOpen(boolean open) {
		this.open = open;
	}
	public boolean isOpen() {
		return open;
	}
	
	@Override
	public T getDefaultValue() {
		return defaultValue;
	}
	
	@Override
	public void setValue(T value) {
		this.value = value;
		setMessage(TextInst.of(value.toString()));
		onChanged.forEach(listener -> listener.onValueChanged(this));
	}
	@Override
	public T getValue() {
		return value;
	}
	@Override
	public boolean isValueValid() {
		return true;
	}
	@Override
	public ConfigValueDropdown<T> addValueListener(ConfigValueListener<ConfigValueDropdown<T>> listener) {
		onChanged.add(listener);
		return this;
	}
	
	@Override
	public int getSpacingWidth() {
		return this.width;
	}
	
	@Override
	public int getSpacingHeight() {
		return this.height;
	}
	
	@Override
	public int getRenderHeight() {
		if (!open)
			return getSpacingHeight();
		
		return allValues.size() * this.height;
	}
	
	@Override
	public ConfigValueDropdown<T> clone(boolean defaults) {
		return new ConfigValueDropdown<>(defaults ? defaultValue : value, defaultValue, allValues, importantValues, open, onChanged);
	}
	
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return false; // Stop space from triggering the button
	}
	
}
