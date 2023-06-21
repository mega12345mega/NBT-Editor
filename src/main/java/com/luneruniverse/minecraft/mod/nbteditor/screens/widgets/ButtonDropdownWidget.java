package com.luneruniverse.minecraft.mod.nbteditor.screens.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionTooltip;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ButtonDropdownWidget extends GroupWidget {
	
	private static class ButtonGrid extends GroupWidget {
		
		private static record QueuedButton(Text msg, ButtonWidget.PressAction onPress, MultiVersionTooltip tooltip) {}
		
		private final int x;
		private final int y;
		private final int gridWidth;
		private final int gridHeight;
		private final Consumer<ButtonGrid> close;
		private final List<QueuedButton> buttons;
		
		public ButtonGrid(int x, int y, int gridWidth, int gridHeight, Consumer<ButtonGrid> close) {
			this.x = x;
			this.y = y;
			this.gridWidth = gridWidth;
			this.gridHeight = gridHeight;
			this.close = close;
			this.buttons = new ArrayList<>();
		}
		
		public void addButton(Text msg, ButtonWidget.PressAction onPress, MultiVersionTooltip tooltip) {
			buttons.add(new QueuedButton(msg, onPress, tooltip));
		}
		
		public void build() {
			clearWidgets();
			
			int columns = (int) Math.ceil(Math.sqrt((double) buttons.size() * gridHeight / gridWidth));
			for (int i = 0; i < buttons.size(); i++) {
				QueuedButton btn = buttons.get(i);
				int gridX = i % columns;
				int gridY = i / columns;
				addWidget(MultiVersionMisc.newButton(x + gridX * gridWidth, y + gridY * gridHeight, gridWidth, gridHeight,
						btn.msg(), btn.onPress(), btn.tooltip()));
			}
		}
		
		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			boolean output = super.mouseClicked(mouseX, mouseY, button);
			if (!output)
				close.accept(this);
			return output;
		}
		
	}
	
	private final ButtonGrid grid;
	private boolean open;
	
	public ButtonDropdownWidget(int x, int y, int btnWidth, int btnHeight, Text msg, MultiVersionTooltip tooltip, int gridWidth, int gridHeight) {
		grid = new ButtonGrid(x, y + btnHeight, gridWidth, gridHeight, grid2 -> setOpen(false));
		addWidget(MultiVersionMisc.newButton(x, y, btnWidth, btnHeight, msg, btn -> setOpen(!open), tooltip));
	}
	public ButtonDropdownWidget(int x, int y, int btnWidth, int btnHeight, Text msg, int gridWidth, int gridHeight) {
		this(x, y, btnWidth, btnHeight, msg, null, gridWidth, gridHeight);
	}
	
	public ButtonDropdownWidget addButton(Text msg, ButtonWidget.PressAction onPress, MultiVersionTooltip tooltip) {
		grid.addButton(msg, onPress, tooltip);
		return this;
	}
	public ButtonDropdownWidget addButton(Text msg, ButtonWidget.PressAction onPress) {
		return addButton(msg, onPress, null);
	}
	
	public ButtonDropdownWidget build() {
		grid.build();
		return this;
	}
	
	public void setOpen(boolean open) {
		if (this.open == open)
			return;
		this.open = open;
		if (open)
			addWidget(grid);
		else
			removeWidget(grid);
	}
	
}
