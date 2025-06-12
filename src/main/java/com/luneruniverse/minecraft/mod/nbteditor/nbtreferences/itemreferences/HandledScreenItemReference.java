package com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences;

import java.util.Optional;
import java.util.function.Consumer;

import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;

public abstract class HandledScreenItemReference implements ItemReference {
	
	public static interface HandledScreenItemReferenceParent {
		public static HandledScreenItemReferenceParent create(Consumer<Optional<ItemStack>> show, Runnable clearCursor) {
			return new HandledScreenItemReferenceParent() {
				@Override
				public void show(Optional<ItemStack> cursor) {
					show.accept(cursor);
				}
				@Override
				public void clearCursor() {
					clearCursor.run();
				}
			};
		}
		public static HandledScreenItemReferenceParent forRoot(HandledScreen<?> screen) {
			return new HandledScreenItemReferenceParent() {
				@Override
				public void show(Optional<ItemStack> cursor) {
					if (MixinLink.CLOSED_SERVER_HANDLED_SCREENS.containsKey(screen)) {
						cursor.ifPresent(MainUtil::setInventoryCursorStack);
						MainUtil.client.player.closeHandledScreen();
						return;
					}
					
					cursor.ifPresent(value -> MainUtil.setRootCursorStack(screen.getScreenHandler(), value));
					MainUtil.client.player.currentScreenHandler = screen.getScreenHandler();
					MainUtil.client.setScreen(screen);
				}
				@Override
				public void clearCursor() {
					if (MixinLink.CLOSED_SERVER_HANDLED_SCREENS.containsKey(screen)) {
						MainUtil.setInventoryCursorStack(ItemStack.EMPTY);
						return;
					}
					
					MainUtil.setRootCursorStack(screen.getScreenHandler(), ItemStack.EMPTY);
				}
			};
		}
		
		public void show(Optional<ItemStack> cursor);
		public void clearCursor();
	}
	
	private HandledScreenItemReferenceParent parent;
	
	public HandledScreenItemReference(HandledScreenItemReferenceParent parent) {
		this.parent = parent;
	}
	public HandledScreenItemReference() {
		this(null);
	}
	
	public HandledScreenItemReference setParent(HandledScreenItemReferenceParent parent) {
		this.parent = parent;
		return this;
	}
	public HandledScreenItemReferenceParent getParent() {
		return parent;
	}
	public HandledScreenItemReferenceParent getDefaultedParent() {
		return (parent == null ? getDefaultParent() : parent);
	}
	public abstract HandledScreenItemReferenceParent getDefaultParent();
	
	@Override
	public void showParent(Optional<ItemStack> cursor) {
		getDefaultedParent().show(cursor);
	}
	
	@Override
	public void escapeParent(Optional<ItemStack> cursor) {
		cursor.ifPresent(MainUtil::setInventoryCursorStack);
		MainUtil.client.player.closeHandledScreen();
	}
	
	@Override
	public void clearParentCursor() {
		getDefaultedParent().clearCursor();
	}
	
}
