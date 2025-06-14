package com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;

import net.minecraft.client.gui.screen.ingame.HandledScreen;

public abstract class HandledScreenItemReference implements ItemReference {
	
	private Runnable parent;
	
	public HandledScreenItemReference() {}
	public HandledScreenItemReference(Runnable parent) {
		this.parent = parent;
	}
	public HandledScreenItemReference(HandledScreen<?> parent) {
		this.parent = () -> NBTEditorClient.CURSOR_MANAGER.showBranch(parent);
	}
	
	public HandledScreenItemReference setParent(Runnable parent) {
		this.parent = parent;
		return this;
	}
	public HandledScreenItemReference setParent(HandledScreen<?> parent) {
		this.parent = () -> NBTEditorClient.CURSOR_MANAGER.showBranch(parent);
		return this;
	}
	public Runnable getParent() {
		return parent;
	}
	public Runnable getDefaultedParent() {
		return (parent == null ? getDefaultParent() : parent);
	}
	public abstract Runnable getDefaultParent();
	
	@Override
	public void showParent() {
		getDefaultedParent().run();
	}
	
	@Override
	public void escapeParent() {
		NBTEditorClient.CURSOR_MANAGER.closeRoot();
	}
	
}
