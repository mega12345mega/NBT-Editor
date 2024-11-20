package com.luneruniverse.minecraft.mod.nbteditor.clientchest;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.item.ItemStack;

public class ClientChestHelper {
	
	private static void trySend(String key) {
		if (MainUtil.client.player != null)
			MainUtil.client.player.sendMessage(TextInst.translatable(key), false);
	}
	
	public static boolean setNameOfPage(int page, String name) {
		try {
			NBTEditorClient.CLIENT_CHEST.setNameOfPage(page, name);
			return true;
		} catch (Exception e) {
			NBTEditor.LOGGER.error("Error saving the client chest!", e);
			trySend("nbteditor.client_chest.save_error");
			return false;
		}
	}
	
	public static CompletableFuture<Boolean> loadDefaultPages(PageLoadLevel loadLevel) {
		try {
			return NBTEditorClient.CLIENT_CHEST.loadDefaultPages(loadLevel).thenApply(v -> true).exceptionally(e -> {
				trySend("nbteditor.client_chest.load_error");
				return false;
			});
		} catch (RuntimeException e) {
			NBTEditor.LOGGER.error("Error loading the client chest!", e);
			trySend("nbteditor.client_chest.load_error");
			return CompletableFuture.completedFuture(false);
		}
	}
	
	public static CompletableFuture<Optional<ClientChestPage>> getPage(int page, PageLoadLevel loadLevel) {
		try {
			return NBTEditorClient.CLIENT_CHEST.getPage(page, loadLevel).thenApply(Optional::of).exceptionally(e -> {
				trySend("nbteditor.client_chest.load_error");
				return Optional.empty();
			});
		} catch (RuntimeException e) {
			NBTEditor.LOGGER.error("Error loading client chest page " + (page + 1), e);
			trySend("nbteditor.client_chest.load_error");
			return CompletableFuture.completedFuture(Optional.empty());
		}
	}
	
	public static CompletableFuture<Boolean> setPage(int page, ItemStack[] items, DynamicItems prevDynamicItems) {
		try {
			return NBTEditorClient.CLIENT_CHEST.setPage(page, items, prevDynamicItems).thenApply(v -> true).exceptionally(e -> {
				trySend("nbteditor.client_chest.save_error");
				return false;
			});
		} catch (RuntimeException e) {
			NBTEditor.LOGGER.error("Error saving client chest page " + (page + 1), e);
			trySend("nbteditor.client_chest.save_error");
			return CompletableFuture.completedFuture(false);
		}
	}
	
	public static CompletableFuture<Boolean> unloadAllPages(PageLoadLevel loadLevel) {
		try {
			return NBTEditorClient.CLIENT_CHEST.unloadAllPages(loadLevel).thenApply(v -> true).exceptionally(e -> {
				trySend("nbteditor.client_chest.unload_error");
				return false;
			});
		} catch (RuntimeException e) {
			NBTEditor.LOGGER.error("Error unloading the client chest!", e);
			trySend("nbteditor.client_chest.unload_error");
			return CompletableFuture.completedFuture(false);
		}
	}
	
	public static CompletableFuture<Optional<ClientChestPage>> unloadPage(int page, PageLoadLevel loadLevel) {
		try {
			return NBTEditorClient.CLIENT_CHEST.unloadPage(page, loadLevel).thenApply(Optional::of).exceptionally(e -> {
				trySend("nbteditor.client_chest.unload_error");
				return Optional.empty();
			});
		} catch (RuntimeException e) {
			NBTEditor.LOGGER.error("Error unloading client chest page " + (page + 1), e);
			trySend("nbteditor.client_chest.unload_error");
			return CompletableFuture.completedFuture(Optional.empty());
		}
	}
	
	public static CompletableFuture<Optional<ClientChestPage>> reloadPage(int page) {
		try {
			return NBTEditorClient.CLIENT_CHEST.reloadPage(page).thenApply(Optional::of).exceptionally(e -> {
				trySend("nbteditor.client_chest.load_error");
				return Optional.empty();
			});
		} catch (RuntimeException e) {
			NBTEditor.LOGGER.error("Error reloading client chest page " + (page + 1), e);
			trySend("nbteditor.client_chest.load_error");
			return CompletableFuture.completedFuture(Optional.empty());
		}
	}
	
	public static CompletableFuture<Boolean> importAllPages() {
		try {
			return NBTEditorClient.CLIENT_CHEST.importAllPages().thenApply(v -> true).exceptionally(e -> {
				trySend("nbteditor.client_chest.import_error");
				return false;
			});
		} catch (RuntimeException e) {
			NBTEditor.LOGGER.error("Error importing the client chest!", e);
			trySend("nbteditor.client_chest.import_error");
			return CompletableFuture.completedFuture(false);
		}
	}
	
	public static CompletableFuture<Boolean> importPage(int page) {
		try {
			return NBTEditorClient.CLIENT_CHEST.importPage(page).thenApply(v -> true).exceptionally(e -> {
				trySend("nbteditor.client_chest.import_error");
				return false;
			});
		} catch (RuntimeException e) {
			NBTEditor.LOGGER.error("Error importing client chest page " + (page + 1), e);
			trySend("nbteditor.client_chest.import_error");
			return CompletableFuture.completedFuture(false);
		}
	}
	
	public static CompletableFuture<Boolean> updateAllPages(Optional<Integer> defaultDataVersion) {
		try {
			return NBTEditorClient.CLIENT_CHEST.updateAllPages(defaultDataVersion).thenApply(v -> true).exceptionally(e -> {
				trySend("nbteditor.client_chest.update_error");
				return false;
			});
		} catch (RuntimeException e) {
			NBTEditor.LOGGER.error("Error updating the client chest!", e);
			trySend("nbteditor.client_chest.update_error");
			return CompletableFuture.completedFuture(false);
		}
	}
	
	public static CompletableFuture<Boolean> updatePage(int page, Optional<Integer> defaultDataVersion) {
		try {
			return NBTEditorClient.CLIENT_CHEST.updatePage(page, defaultDataVersion).thenApply(v -> true).exceptionally(e -> {
				trySend("nbteditor.client_chest.update_error");
				return false;
			});
		} catch (RuntimeException e) {
			NBTEditor.LOGGER.error("Error updating client chest page " + (page + 1), e);
			trySend("nbteditor.client_chest.update_error");
			return CompletableFuture.completedFuture(false);
		}
	}
	
	public static CompletableFuture<Boolean> discardPage(int page) {
		try {
			return NBTEditorClient.CLIENT_CHEST.discardPage(page).thenApply(v -> true).exceptionally(e -> {
				trySend("nbteditor.client_chest.save_error");
				return false;
			});
		} catch (RuntimeException e) {
			NBTEditor.LOGGER.error("Error discarding client chest page " + (page + 1), e);
			trySend("nbteditor.client_chest.save_error");
			return CompletableFuture.completedFuture(false);
		}
	}
	
}
