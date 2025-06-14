package com.luneruniverse.minecraft.mod.nbteditor.screens.containers;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.MVClientNetworking;
import com.luneruniverse.minecraft.mod.nbteditor.packets.SetCursorC2SPacket;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

public class CursorManager {
	
	private HandledScreen<?> currentRoot;
	private boolean currentRootIsInventory;
	private boolean currentRootHasServerCursor;
	private boolean currentRootClosed;
	private HandledScreen<?> currentBranch;
	
	public CursorManager() {}
	
	public boolean isBranched() {
		return currentRoot != null && currentRoot != currentBranch;
	}
	public HandledScreen<?> getCurrentRoot() {
		return currentRoot;
	}
	public boolean isCurrentRootClosed() {
		return currentRootClosed;
	}
	public HandledScreen<?> getCurrentBranch() {
		return currentBranch;
	}
	
	public void onNoScreenSet() {
		currentRoot = null;
		currentRootClosed = false;
		currentBranch = null;
	}
	
	public void onHandledScreenSet(HandledScreen<?> screen) {
		if (screen == currentBranch)
			return;
		
		currentRoot = screen;
		currentRootIsInventory = (currentRoot.getScreenHandler() == MainUtil.client.player.playerScreenHandler ||
				currentRoot instanceof CreativeInventoryScreen);
		currentRootHasServerCursor = !(screen instanceof CreativeInventoryScreen);
		currentRootClosed = false;
		currentBranch = screen;
	}
	
	public void onCloseScreenPacket() {
		if (currentRoot == null || currentRootIsInventory)
			return;
		
		currentRootClosed = true;
	}
	
	private void transferCursorTo(HandledScreen<?> branch) {
		if (currentBranch == branch)
			return;
		
		ScreenHandler handler = branch.getScreenHandler();
		ScreenHandler currentHandler = currentBranch.getScreenHandler();
		
		handler.setCursorStack(currentHandler.getCursorStack());
		handler.setPreviousCursorStack(handler.getCursorStack());
		
		currentHandler.setCursorStack(ItemStack.EMPTY);
		currentHandler.setPreviousCursorStack(ItemStack.EMPTY);
		
		if (currentRootHasServerCursor) {
			if (branch == currentRoot)
				MVClientNetworking.send(new SetCursorC2SPacket(handler.getCursorStack()));
			else if (currentBranch == currentRoot)
				MVClientNetworking.send(new SetCursorC2SPacket(ItemStack.EMPTY));
		}
	}
	
	public void showBranch(HandledScreen<?> branch) {
		if (currentRoot == null) {
			if (MainUtil.client.interactionManager.hasCreativeInventory()) {
				currentRoot = MVMisc.newCreativeInventoryScreen(MainUtil.client.player);
				currentRootHasServerCursor = false;
			} else {
				currentRoot = new InventoryScreen(MainUtil.client.player);
				currentRootHasServerCursor = true;
			}
			currentRootIsInventory = true;
			currentRootClosed = false;
			currentBranch = currentRoot;
		}
		if (branch == null)
			branch = currentRoot;
		
		if (currentRootClosed && branch == currentRoot) {
			closeRoot();
			return;
		}
		
		transferCursorTo(branch);
		currentBranch = branch;
		MainUtil.client.player.currentScreenHandler = branch.getScreenHandler();
		branch.cancelNextRelease = true;
		MainUtil.client.setScreen(branch);
	}
	public void showRoot() {
		showBranch(currentRoot);
	}
	
	public void closeRoot() {
		if (currentRoot == null) {
			MainUtil.client.setScreen(null);
			return;
		}
		
		if (currentRootClosed) {
			ItemStack cursor = currentBranch.getScreenHandler().getCursorStack();
			if (currentRootHasServerCursor) {
				MainUtil.get(cursor, true);
				cursor = ItemStack.EMPTY;
			}
			currentRoot.getScreenHandler().setCursorStack(cursor);
			currentRoot.getScreenHandler().setPreviousCursorStack(cursor);
			MainUtil.client.player.closeScreen(); // will trigger #onNoScreenSet()
			return;
		}
		
		transferCursorTo(currentRoot);
		MainUtil.client.player.closeHandledScreen(); // will trigger #onNoScreenSet()
	}
	
}
