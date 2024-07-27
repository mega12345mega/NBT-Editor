package com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences;

import java.util.Optional;

import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;

public abstract class HandledScreenItemReference implements ItemReference {
	
	private HandledScreen<?> parent;
	
	public HandledScreenItemReference(HandledScreen<?> parent) {
		this.parent = parent;
	}
	public HandledScreenItemReference() {
		this(null);
	}
	
	public HandledScreenItemReference setParent(HandledScreen<?> parent) {
		this.parent = parent;
		return this;
	}
	public HandledScreen<?> getParent() {
		return parent;
	}
	public HandledScreen<?> getDefaultedParent() {
		return (parent == null ? getDefaultParent() : parent);
	}
	public abstract HandledScreen<?> getDefaultParent();
	
	@Override
	public void showParent(Optional<ItemStack> cursor) {
		HandledScreen<?> newScreen = getDefaultedParent();
		cursor.ifPresent(value -> MainUtil.setRootCursorStack(newScreen.getScreenHandler(), value));
		MainUtil.client.player.currentScreenHandler = newScreen.getScreenHandler();
		MainUtil.client.setScreen(newScreen);
	}
	
	@Override
	public void escapeParent(Optional<ItemStack> cursor) {
		cursor.ifPresent(value -> MainUtil.setRootCursorStack(getDefaultedParent().getScreenHandler(), value));
		MainUtil.client.player.closeHandledScreen();
	}
	
	@Override
	public void clearParentCursor() {
		MainUtil.setRootCursorStack(getDefaultedParent().getScreenHandler(), ItemStack.EMPTY);
	}
	
}
