package com.luneruniverse.minecraft.mod.nbteditor.screens.configurable;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.lwjgl.glfw.GLFW;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVTooltip;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.InputOverlay;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.StringInput;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ConfigList extends ConfigGroupingVertical<Integer, ConfigList> {
	
	private static class ConfigListEntry implements ConfigPath {
		
		public enum ListContextMenuAction {
			MOVE("nbteditor.configurable.list.move"),
			DUPLICATE("nbteditor.configurable.list.duplicate", "nbteditor.configurable.list.duplicate.any_amount"),
			REMOVE("nbteditor.configurable.list.remove");
			
			private final Text msg;
			private final Text tooltip;
			private ListContextMenuAction(String msg, String tooltip) {
				this.msg = TextInst.translatable(msg);
				this.tooltip = tooltip == null ? null : TextInst.translatable(tooltip);
			}
			private ListContextMenuAction(String msg) {
				this(msg, null);
			}
		}
		
		private static final int LIST_CONTEXT_MENU_HEIGHT = (MainUtil.client.textRenderer.fontHeight + 2) * 3 + 2;
		
		private final ConfigList parent;
		private final ConfigPath value;
		private final boolean named;
		private final boolean indexed;
		private int index;
		private Text indexText;
		private int indexTextOffset;
		
		private boolean contextMenuOpen;
		private int contextMenuX;
		private int contextMenuY;
		
		public ConfigListEntry(ConfigList parent, ConfigPath value, int index, boolean indexed) {
			this.parent = parent;
			this.value = value;
			this.indexed = indexed;
			this.named = value instanceof ConfigPathNamed;
			setIndex(index);
			
			if (named)
				((ConfigPathNamed) value).setNamePrefix(indexText);
		}
		
		public void setIndex(int index) {
			this.index = index;
			if (indexed) {
				this.indexText = TextInst.literal("(#" + (index + 1) + ") ");
				this.indexTextOffset = named ? 0 : MainUtil.client.textRenderer.getWidth(this.indexText);
				if (named)
					((ConfigPathNamed) value).setNamePrefix(this.indexText);
			} else {
				this.indexText = TextInst.of("");
				this.indexTextOffset = 0;
			}
		}
		
		public void duplicate(int numCopies) {
			for (int dupe = 0; dupe < numCopies; dupe++) {
				for (int i = parent.paths.size() - 2; i > index; i--) {
					ConfigListEntry entry = parent.getListEntry(i);
					entry.setIndex(i + 1);
					parent.setListEntry(i + 1, entry);
				}
				ConfigListEntry clone = this.clone(false);
				clone.setIndex(index + 1);
				parent.setListEntry(index + 1, clone);
			}
			parent.onChanged.forEach(listener -> listener.onValueChanged(null));
		}
		
		@Override
		public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
			if (named)
				value.render(matrices, mouseX, mouseY, delta);
			else {
				MVDrawableHelper.drawTextWithShadow(matrices, MainUtil.client.textRenderer, indexText, 0, (getSpacingHeight() - MainUtil.client.textRenderer.fontHeight) / 2, -1);
				matrices.push();
				matrices.translate(indexTextOffset, 0.0, 0.0);
				value.render(matrices, mouseX - indexTextOffset, mouseY, delta);
				matrices.pop();
			}
		}
		
		public void renderContextMenu(MatrixStack matrices, int mouseX, int mouseY, float delta) {
			if (!contextMenuOpen)
				return;
			
			matrices.push();
			matrices.translate(0.0, 0.0, 1.0);
			
			MVDrawableHelper.fill(matrices, contextMenuX - 1, contextMenuY - 1, contextMenuX + 51, contextMenuY + LIST_CONTEXT_MENU_HEIGHT + 1, -1);
			MVDrawableHelper.fill(matrices, contextMenuX, contextMenuY, contextMenuX + 50, contextMenuY + LIST_CONTEXT_MENU_HEIGHT, 0xFF000000);
			boolean xHover = mouseX > contextMenuX && mouseX < contextMenuX + 50; // Prevent the first option from being hovered before moving the mouse
			int y = contextMenuY;
			for (ListContextMenuAction action : ListContextMenuAction.values()) {
				int color = -1;
				if (xHover && mouseY >= y && mouseY <= y + MainUtil.client.textRenderer.fontHeight) {
					color = 0xFF257789;
					if (action.tooltip != null && !ConfigScreen.isKeybindsHidden())
						new MVTooltip(action.tooltip).render(matrices, mouseX, mouseY);
				}
				Text msg = action.msg;
				if (action == ListContextMenuAction.REMOVE)
					msg = TextInst.copy(msg).formatted(color == -1 ? Formatting.RED : Formatting.GOLD);
				MVDrawableHelper.drawCenteredTextWithShadow(matrices, MainUtil.client.textRenderer, msg, contextMenuX + 25, y + 2, color);
				y += MainUtil.client.textRenderer.fontHeight + 2;
			}
			
			matrices.pop();
		}
		
		@Override
		public boolean isValueValid() {
			return value.isValueValid();
		}
		@Override
		public ConfigListEntry addValueListener(ConfigValueListener<ConfigValue<?, ?>> listener) {
			value.addValueListener(listener);
			return this;
		}
		
		@Override
		public int getSpacingWidth() {
			return indexTextOffset + value.getSpacingWidth();
		}
		
		@Override
		public int getSpacingHeight() {
			return Math.max(20, value.getSpacingHeight());
		}
		
		@Override
		public int getRenderWidth() {
			return indexTextOffset + value.getRenderWidth();
		}
		
		@Override
		public int getRenderHeight() {
			return Math.max(getSpacingHeight(), value.getRenderHeight());
		}
		
		@Override
		public ConfigListEntry clone(boolean defaults) {
			return new ConfigListEntry(parent, value.clone(defaults), index, indexed);
		}
		
		
		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (contextMenuOpen) {
				if (mouseX >= contextMenuX && mouseX <= contextMenuX + 50 && mouseY >= contextMenuY && mouseY <= contextMenuY + LIST_CONTEXT_MENU_HEIGHT) {
					if (mouseX > contextMenuX && mouseX < contextMenuX + 50) {
						int y = contextMenuY;
						for (ListContextMenuAction action : ListContextMenuAction.values()) {
							if (mouseY >= y && mouseY <= y + MainUtil.client.textRenderer.fontHeight) {
								switch (action) {
									case MOVE -> {
										InputOverlay.show(
												TextInst.translatable("nbteditor.configurable.list.move"),
												StringInput.builder()
														.withDefault(index + 1 + "")
														.withPlaceholder(
																TextInst.translatable("nbteditor.configurable.list.move.index"))
														.withValidator(
																MainUtil.intPredicate(() -> 1, () -> parent.paths.size() - 1, false))
														.build(),
												str -> {
													int target = Integer.parseInt(str) - 1;
													if (target == index)
														return;
													int dir = (index < target ? 1 : -1);
													for (int i = index; dir == 1 ? i < target : i > target; i += dir) {
														ConfigListEntry entry = parent.getListEntry(i + dir);
														entry.setIndex(i);
														parent.setListEntry(i, entry);
													}
													setIndex(target);
													parent.setListEntry(target, this);
													parent.onChanged.forEach(listener -> listener.onValueChanged(null));
												});
									}
									case DUPLICATE -> {
										if (Screen.hasShiftDown()) {
											InputOverlay.show(
													TextInst.translatable("nbteditor.configurable.list.duplicate"),
													StringInput.builder()
															.withPlaceholder(
																	TextInst.translatable("nbteditor.configurable.list.duplicate.amount"))
															.withValidator(
																	MainUtil.intPredicate(1, Integer.MAX_VALUE, false))
															.build(),
													numCopies -> duplicate(Integer.parseInt(numCopies)));
										} else
											duplicate(1);
									}
									case REMOVE -> {
										for (int i = index; i < parent.paths.size() - 2; i++) {
											ConfigListEntry entry = parent.getListEntry(i + 1);
											entry.setIndex(i);
											parent.setListEntry(i, entry);
										}
										parent.paths.remove(parent.paths.size() - 2);
										parent.onChanged.forEach(listener -> listener.onValueChanged(null));
									}
								}
								contextMenuOpen = false;
								break;
							}
							y += MainUtil.client.textRenderer.fontHeight + 2;
						}
					}
					return true;
				} else
					contextMenuOpen = false;
			}
			
			int height = getSpacingHeight();
			if (mouseX >= -PADDING * 2 && mouseX <= -PADDING && mouseY >= 0 && mouseY <= height) { // Click on the bar
				if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
					if (mouseY <= PADDING) { // Move up
						if (index > 0) {
							ConfigListEntry above = parent.getListEntry(index - 1);
							above.setIndex(index);
							parent.setListEntry(index, above);
							
							setIndex(index - 1);
							parent.setListEntry(index, this);
							parent.onChanged.forEach(listener -> listener.onValueChanged(null));
						}
						return true;
					} else if (mouseY >= height - PADDING) { // Move down
						if (index < parent.paths.size() - 2) { // Account for '+' button
							ConfigListEntry below = parent.getListEntry(index + 1);
							below.setIndex(index);
							parent.setListEntry(index, below);
							
							setIndex(index + 1);
							parent.setListEntry(index, this);
							parent.onChanged.forEach(listener -> listener.onValueChanged(null));
						}
						return true;
					} else {
						contextMenuOpen = true;
						contextMenuX = (int) mouseX;
						contextMenuY = (int) mouseY;
						return true;
					}
				} else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
					contextMenuOpen = true;
					contextMenuX = (int) mouseX;
					contextMenuY = (int) mouseY;
					return true;
				}
			}
			
			return value.mouseClicked(mouseX - indexTextOffset, mouseY, button);
		}
		@Override
		public boolean mouseReleased(double mouseX, double mouseY, int button) {
			return value.mouseReleased(mouseX - indexTextOffset, mouseY, button);
		}
		@Override
		public void mouseMoved(double mouseX, double mouseY) {
			value.mouseMoved(mouseX - indexTextOffset, mouseY);
		}
		@Override
		public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
			return value.mouseDragged(mouseX - indexTextOffset, mouseY, button, deltaX, deltaY);
		}
		@Override
		public boolean mouseScrolled(double mouseX, double mouseY, double xAmount, double yAmount) {
			return value.mouseScrolled(mouseX - indexTextOffset, mouseY, xAmount, yAmount);
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
		
		@Override
		public void tick() {
			value.tick();
		}
		
	}
	
	private final boolean indexed;
	
	public ConfigList(Text name, boolean indexed, ConfigPath defaultEntry) {
		super(name, name2 -> new ConfigList(name2, indexed, defaultEntry));
		this.indexed = indexed;
		
		super.setSorter((a, b) -> a - b);
		super.setConfigurable(-1, new ConfigButton(20, TextInst.of("+"), btn -> {
			addConfigurable(defaultEntry.clone(true));
			onChanged.forEach(listener -> listener.onValueChanged(null));
		}));
	}
	public ConfigList(boolean indexed, ConfigPath defaultEntry) {
		this(null, indexed, defaultEntry);
	}
	public ConfigList(ConfigPath defaultEntry) {
		this(null, false, defaultEntry);
	}
	
	@Override
	public ConfigList setConfigurable(Integer key, ConfigPath path) {
		if (key == null)
			throw new IllegalArgumentException("The key cannot be null");
		if (key < 0 || key >= paths.size())
			throw new ArrayIndexOutOfBoundsException(key);
		
		super.setConfigurable(key, new ConfigListEntry(this, path, paths.size() - 1, indexed));
		path.setParent(this);
		return this;
	}
	@Override
	public ConfigPath getConfigurable(Integer key) {
		ConfigPath output = super.getConfigurable(key);
		if (output == null)
			return null;
		return ((ConfigListEntry) output).value;
	}
	@Override
	public Map<Integer, ConfigPath> getConfigurables() {
		return paths.entrySet().stream().filter(entry -> entry.getKey() >= 0).collect(Collectors.toMap(
				Map.Entry::getKey, entry -> ((ConfigListEntry) entry.getValue()).value, (a, b) -> a, LinkedHashMap::new));
	}
	@Override
	public ConfigList sort(Comparator<Integer> sorter) {
		Map<Integer, ConfigPath> indexes = new TreeMap<>(sorter);
		for (int i = 0; i < paths.size() - 1; i++)
			indexes.put(i, paths.get(i));
		int i = 0;
		for (ConfigPath path : indexes.values()) {
			((ConfigListEntry) path).setIndex(i);
			paths.put(i, path);
			i++;
		}
		return this;
	}
	@Override
	public ConfigList setSorter(Comparator<Integer> sorter) {
		throw new UnsupportedOperationException("Lists are always sorted by their index! Use sort instead.");
	}
	
	public ConfigList addConfigurable(ConfigPath path) {
		return setConfigurable(paths.size() - 1, path);
	}
	
	private void setListEntry(int key, ConfigListEntry entry) {
		paths.put(key, entry);
	}
	private ConfigListEntry getListEntry(int key) {
		return (ConfigListEntry) paths.get(key);
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);
		
		if (isValueValid()) {
			int yOffset = getNameHeight();
			for (ConfigPath path : paths.values()) {
				int height = path.getSpacingHeight();
				if (path instanceof ConfigListEntry) {
					MVDrawableHelper.fill(matrices, 0, yOffset, PADDING, yOffset + height, 0xFF000000);
					MVDrawableHelper.fill(matrices, 0, yOffset + PADDING, PADDING, yOffset + height - PADDING, 0xFF257789);
					
					matrices.push();
					matrices.translate(-PADDING / 2, -(yOffset + height / 2), 0.0);
					matrices.scale(2, 2, 1);
					matrices.translate(PADDING / 2 - 0.5, yOffset + height / 2, 0.0);
					MVDrawableHelper.drawTextWithShadow(matrices, MainUtil.client.textRenderer, TextInst.of("⋮"), 0,
							-MainUtil.client.textRenderer.fontHeight / 2, -1);
					matrices.pop();
				}
				yOffset += height + PADDING;
			}
		}
		
		int yOffset = getNameHeight();
		for (ConfigPath path : paths.values()) {
			if (path instanceof ConfigListEntry entry) {
				matrices.push();
				matrices.translate(PADDING * 2, yOffset, 0.0);
				entry.renderContextMenu(matrices, mouseX - PADDING * 2, mouseY - yOffset, delta);
				matrices.pop();
			}
			yOffset += path.getSpacingHeight() + PADDING;
		}
	}
	
}
