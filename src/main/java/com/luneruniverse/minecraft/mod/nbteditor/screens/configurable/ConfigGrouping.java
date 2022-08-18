package com.luneruniverse.minecraft.mod.nbteditor.screens.configurable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.luneruniverse.minecraft.mod.nbteditor.util.OrderedMap;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public abstract class ConfigGrouping<K, T extends ConfigGrouping<K, T>> implements ConfigPathNamed {
	
	protected interface Constructor<K, T extends ConfigGrouping<K, T>> {
		ConfigGrouping<K, T> newInstance(Text name);
	}
	
	protected final Text name;
	protected final OrderedMap<K, ConfigPath> paths;
	private final Constructor<K, T> cloneImpl;
	
	protected Text namePrefix;
	protected final List<ConfigValueListener<ConfigValue<?, ?>>> onChanged;
	
	protected ConfigGrouping(Text name, Constructor<K, T> cloneImpl) {
		this.name = name;
		this.paths = new OrderedMap<>();
		this.cloneImpl = cloneImpl;
		this.onChanged = new ArrayList<>();
	}
	
	public Text getName() {
		return name;
	}
	protected int getNameHeight() {
		return name == null ? 0 : MainUtil.client.textRenderer.fontHeight + PADDING;
	}
	@Override
	public void setNamePrefix(Text prefix) {
		namePrefix = prefix;
	}
	@Override
	public Text getNamePrefix() {
		return namePrefix;
	}
	
	@SuppressWarnings("unchecked")
	public T setConfigurable(K key, ConfigPath path) {
		paths.put(key, path);
		path.addValueListener(source -> onChanged.forEach(listener -> listener.onValueChanged(source)));
		return (T) this;
	}
	public ConfigPath getConfigurable(K key) {
		return paths.get(key);
	}
	public Map<K, ConfigPath> getConfigurables() {
		return Collections.unmodifiableMap(paths);
	}
	@SuppressWarnings("unchecked")
	public T sort(Comparator<K> sorter) {
		paths.sort(sorter);
		return (T) this;
	}
	@SuppressWarnings("unchecked")
	public T setSorter(Comparator<K> sorter) {
		paths.setSorter(sorter);
		return (T) this;
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		DrawableHelper.fill(matrices, 0, 0, PADDING, getSpacingHeight(), isValueValid() ? 0xFFAAAAAA : 0xFFDF4949);
		
		int yOffset = 0;
		Text fullName = getFullName();
		if (fullName != null) {
			DrawableHelper.drawTextWithShadow(matrices, MainUtil.client.textRenderer, fullName, PADDING * 2, 0, 0xFFFFFFFF);
			yOffset += getNameHeight();
		}
		
		// Render in reverse order to allow dropdowns to display over below components
		List<ConfigPath> paths = new ArrayList<>(this.paths.values());
		Collections.reverse(paths);
		for (ConfigPath path : paths)
			yOffset += path.getSpacingHeight() + PADDING;
		
		for (ConfigPath path : paths) {
			yOffset -= path.getSpacingHeight() + PADDING;
			
			matrices.push();
			matrices.translate(PADDING * 2, yOffset, 0);
			path.render(matrices, mouseX - PADDING * 2, mouseY - yOffset, delta);
			matrices.pop();
		}
	}
	
	@Override
	public boolean isValueValid() {
		return paths.values().stream().allMatch(ConfigPath::isValueValid);
	}
	@SuppressWarnings("unchecked")
	@Override
	public T addValueListener(ConfigValueListener<ConfigValue<?, ?>> listener) {
		onChanged.add(listener);
		return (T) this;
	}
	
	@Override
	public int getSpacingHeight() {
		return getNameHeight() + paths.values().stream().mapToInt(Configurable::getSpacingHeight).reduce((a, b) -> a + PADDING + b).orElse(0);
	}
	
	@Override
	public int getRenderHeight() {
		int output = getNameHeight();
		int yOffset = output;
		for (ConfigPath path : paths.values()) {
			int bottomY = yOffset + path.getRenderHeight();
			if (bottomY > output)
				output = bottomY;
			yOffset += path.getSpacingHeight() + PADDING;
		}
		return output;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T clone(boolean defaults) {
		T output = (T) cloneImpl.newInstance(name);
		paths.forEach((key, path) -> output.setConfigurable(key, path.clone(defaults)));
		output.onChanged.addAll(onChanged);
		return output;
	}
	
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		int yOffset = getNameHeight();
		
		for (ConfigPath path : new ArrayList<>(paths.values())) {
			if (path.mouseClicked(mouseX - PADDING * 2, mouseY - yOffset, button))
				return true;
			yOffset += path.getSpacingHeight() + PADDING;
		}
		return false;
	}
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		int yOffset = getNameHeight();
		
		for (ConfigPath path : new ArrayList<>(paths.values())) {
			if (path.mouseReleased(mouseX - PADDING * 2, mouseY - yOffset, button))
				return true;
			yOffset += path.getSpacingHeight() + PADDING;
		}
		return false;
	}
	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		int yOffset = getNameHeight();
		
		for (ConfigPath path : new ArrayList<>(paths.values())) {
			path.mouseMoved(mouseX - PADDING * 2, mouseY - yOffset);
			yOffset += path.getSpacingHeight() + PADDING;
		}
	}
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		int yOffset = getNameHeight();
		
		for (ConfigPath path : new ArrayList<>(paths.values())) {
			if (path.mouseDragged(mouseX - PADDING * 2, mouseY - yOffset, button, deltaX, deltaY))
				return true;
			yOffset += path.getSpacingHeight() + PADDING;
		}
		return false;
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		int yOffset = getNameHeight();
		
		for (ConfigPath path : new ArrayList<>(paths.values())) {
			if (path.mouseScrolled(mouseX - PADDING * 2, mouseY - yOffset, amount))
				return true;
			yOffset += path.getSpacingHeight() + PADDING;
		}
		return false;
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		for (ConfigPath path : new ArrayList<>(paths.values())) {
			if (path.keyPressed(keyCode, scanCode, modifiers))
				return true;
		}
		return false;
	}
	@Override
	public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		for (ConfigPath path : new ArrayList<>(paths.values())) {
			if (path.keyReleased(keyCode, scanCode, modifiers))
				return true;
		}
		return false;
	}
	@Override
	public boolean charTyped(char chr, int modifiers) {
		for (ConfigPath path : new ArrayList<>(paths.values())) {
			if (path.charTyped(chr, modifiers))
				return true;
		}
		return false;
	}
	
}
