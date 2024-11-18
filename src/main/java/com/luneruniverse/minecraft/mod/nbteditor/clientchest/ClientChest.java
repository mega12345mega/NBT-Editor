package com.luneruniverse.minecraft.mod.nbteditor.clientchest;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.DataVersionStatus;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.EditableText;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.NBTManagers;
import com.luneruniverse.minecraft.mod.nbteditor.util.LoadQueue;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.luneruniverse.minecraft.mod.nbteditor.util.PartitionedLock;
import com.luneruniverse.minecraft.mod.nbteditor.util.SaveQueue;

import net.minecraft.datafixer.TypeReferences;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;

public class ClientChest {
	
	public static final File CLIENT_CHEST_FOLDER = new File(NBTEditorClient.SETTINGS_FOLDER, "client_chest");
	private static final File PAGE_NAMES = new File(CLIENT_CHEST_FOLDER, "page_names.json");
	
	private volatile ClientChestPageCache cache;
	private final Queue<Integer> cachePageCounts;
	
	private final Map<String, Integer> nameToPage;
	private final Map<Integer, String> pageToName;
	
	private final PartitionedLock lock;
	private final LoadingCache<Integer, LoadQueue<ClientChestPage>> loadQueues;
	private final LoadingCache<Integer, SaveQueue<ClientChestPage>> saveQueues;
	private final Map<Integer, Integer> uncachedProcessers;
	
	@SuppressWarnings("serial")
	public ClientChest(ClientChestPageCache cache) {
		this.cache = cache;
		this.cachePageCounts = new ConcurrentLinkedQueue<>();
		
		if (PAGE_NAMES.exists()) {
			try (FileReader reader = new FileReader(PAGE_NAMES, Charset.forName("UTF-8"))) {
				nameToPage = new Gson().fromJson(reader, new TypeToken<Map<String, Integer>>() {}.getType());
			} catch (Exception e) {
				throw new RuntimeException("Unable to read page names!", e);
			}
			pageToName = new HashMap<>();
			nameToPage.forEach((name, page) -> pageToName.put(page, name));
		} else {
			nameToPage = new HashMap<>();
			pageToName = new HashMap<>();
		}
		
		lock = new PartitionedLock();
		loadQueues = CacheBuilder.newBuilder().weakValues().build(new CacheLoader<>() {
			@Override
			public LoadQueue<ClientChestPage> load(Integer page) {
				return new LoadQueue<>("ClientChest/Loading", level -> {
					lock.lock(page);
					try {
						return readPageSync(page, PageLoadLevel.values()[level]);
					} catch (Throwable e) {
						NBTEditor.LOGGER.error("Error loading client chest page " + (page + 1), e);
						if (e instanceof Error error)
							throw error;
						backupCorruptPage(page);
						ClientChest.this.cache.cacheEmptyPage(page);
						return new ClientChestPage();
					} finally {
						lock.unlock(page);
					}
				}, true);
			}
		});
		saveQueues = CacheBuilder.newBuilder().weakValues().build(new CacheLoader<>() {
			@Override
			public SaveQueue<ClientChestPage> load(Integer page) {
				return new SaveQueue<>("ClientChest/Saving", pageData -> {
					lock.lock(page);
					try {
						writePageSync(page, pageData);
					} catch (Throwable e) {
						NBTEditor.LOGGER.error("Error saving client chest page " + (page + 1), e);
						if (e instanceof Error error)
							throw error;
						throw new RuntimeException("Error saving client chest page " + (page + 1), e);
					} finally {
						lock.unlock(page);
					}
				}, true);
			}
		});
		uncachedProcessers = new ConcurrentHashMap<>();
	}
	
	public CompletableFuture<Void> setCache(ClientChestPageCache cache) {
		cachePageCounts.add(cache.getPageCount());
		
		CompletableFuture<Void> future = new CompletableFuture<>();
		Thread thread = new Thread(() -> {
			lock.lockAll();
			try {
				this.cache.transferTo(cache);
				this.cache = cache;
				cachePageCounts.remove();
				future.complete(null);
			} finally {
				lock.unlockAll();
			}
		}, "NBTEditor/Async/ClientChest/SwitchingCache");
		thread.start();
		return future;
	}
	public ClientChestPageCache getCache() {
		return cache;
	}
	public int getPageCount() {
		return Math.min(cache.getPageCount(), cachePageCounts.stream().mapToInt(pageCount -> pageCount).min().orElse(Integer.MAX_VALUE));
	}
	
	public Integer getPageFromName(String name) {
		return nameToPage.get(name);
	}
	public String getNameFromPage(int page) {
		return pageToName.getOrDefault(page, "");
	}
	public List<String> getAllPageNames(boolean withinPageCount) {
		if (!withinPageCount)
			return new ArrayList<>(nameToPage.keySet());
		return nameToPage.entrySet().stream().filter(entry -> entry.getValue() < getPageCount()).map(Map.Entry::getKey).toList();
	}
	public boolean isNameUsedByOther(String name, int page) {
		return nameToPage.getOrDefault(name, page) != page;
	}
	@SuppressWarnings("serial")
	public void setNameOfPage(int page, String name) throws Exception {
		if (Objects.equals(pageToName.get(page), name != null && name.isEmpty() ? null : name))
			return;
		if (name == null || name.isEmpty()) {
			nameToPage.remove(pageToName.remove(page));
		} else {
			nameToPage.remove(pageToName.get(page));
			Integer oldPage = nameToPage.put(name, page);
			if (oldPage != null && oldPage != page)
				pageToName.remove(oldPage);
			pageToName.put(page, name);
		}
		if (!CLIENT_CHEST_FOLDER.exists())
			CLIENT_CHEST_FOLDER.mkdir();
		try (FileWriter writer = new FileWriter(PAGE_NAMES, Charset.forName("UTF-8"))) {
			new Gson().toJson(nameToPage, new TypeToken<Map<Integer, String>>() {}.getType(), writer);
		}
	}
	
	public void stop() {
		lock.stop();
	}
	
	public boolean isProcessingPage(int page) {
		if (lock.isLocked(page) || isUncachedProcessingPage(page))
			return true;
		
		LoadQueue<ClientChestPage> loadQueue = loadQueues.getIfPresent(page);
		if (loadQueue != null && loadQueue.isLoading())
			return true;
		
		SaveQueue<ClientChestPage> saveQueue = saveQueues.getIfPresent(page);
		if (saveQueue != null && saveQueue.isSaving())
			return true;
		
		return false;
	}
	public boolean isUncachedProcessingPage(int page) {
		return uncachedProcessers.getOrDefault(page, 0) > 0 || uncachedProcessers.getOrDefault(-1, 0) > 0;
	}
	
	public CompletableFuture<Void> loadDefaultPages(PageLoadLevel level) {
		if (level == PageLoadLevel.UNLOADED)
			return CompletableFuture.completedFuture(null);
		
		CompletableFuture<Void> future = new CompletableFuture<>();
		Thread thread = new Thread(() -> {
			lock.lockAll();
			try {
				Exception toThrow = new Exception("Error loading page(s)");
				for (int i = 0; i < cache.getDefaultLoadedPagesCount(); i++) {
					try {
						readPageSync(i, level);
					} catch (Throwable e) {
						toThrow.addSuppressed(new Exception("Page " + (i + 1), e));
						if (e instanceof Error)
							continue;
						backupCorruptPage(i);
						cache.cacheEmptyPage(i);
					}
				}
				if (toThrow.getSuppressed().length > 0) {
					NBTEditor.LOGGER.error("Error loading the client chest!", toThrow);
					future.completeExceptionally(toThrow);
				} else
					future.complete(null);
			} finally {
				lock.unlockAll();
			}
		}, "NBTEditor/Async/ClientChest/Loading");
		thread.setDaemon(true);
		thread.start();
		return future;
	}
	
	public PageLoadLevel getLoadLevel(int page) {
		return cache.getCachedPage(page).loadLevel();
	}
	
	public CompletableFuture<ClientChestPage> getPage(int page, PageLoadLevel loadLevel) {
		if (!isUncachedProcessingPage(page)) {
			ClientChestPage cachedPage = cache.getCachedPage(page);
			if (loadLevel.ordinal() <= cachedPage.loadLevel().ordinal())
				return CompletableFuture.completedFuture(cachedPage);
		}
		
		return loadQueues.getUnchecked(page).load(loadLevel.ordinal());
	}
	
	public CompletableFuture<Void> setPage(int page, ItemStack[] items, DynamicItems prevDynamicItems) {
		Optional<DataVersionStatus> dataVersionStatus = getDataVersionStatus(page);
		if (dataVersionStatus.isEmpty()) {
			try {
				dataVersionStatus = Optional.of(readDataVersionStatusSync(page));
			} catch (Exception e) {
				NBTEditor.LOGGER.error("Error saving client chest page " + (page + 1), e);
				return CompletableFuture.failedFuture(e);
			}
		}
		if (dataVersionStatus.get() != DataVersionStatus.CURRENT)
			throw new IllegalStateException("Cannot write to a page which has a different DataVersion!");
		
		boolean allAir = prevDynamicItems.getLockedSlots().isEmpty();
		if (allAir) {
			for (ItemStack item : items) {
				if (item != null && !item.isEmpty()) {
					allAir = false;
					break;
				}
			}
		}
		if (allAir) {
			cache.cacheEmptyPage(page);
			try {
				Files.deleteIfExists(getFile(page).toPath());
			} catch (Exception e) {
				NBTEditor.LOGGER.error("Error saving client chest page " + (page + 1), e);
				return CompletableFuture.failedFuture(e);
			}
			return CompletableFuture.completedFuture(null);
		}
		
		DynamicItems dynamicItems = new DynamicItems();
		for (int i = 0; i < items.length; i++) {
			if (prevDynamicItems.isSlotLocked(i))
				dynamicItems.add(i, prevDynamicItems.getOriginalNbt(i), false);
			else if (items[i] != null)
				items[i] = dynamicItems.tryAdd(i, items[i]);
		}
		
		ClientChestPage pageData = new ClientChestPage(items, dynamicItems, PageLoadLevel.DYNAMIC_ITEMS);
		cache.cachePage(page, pageData);
		
		return saveQueues.getUnchecked(page).save(pageData);
	}
	
	public CompletableFuture<Void> unloadAllPages(PageLoadLevel loadLevel) {
		if (loadLevel == PageLoadLevel.DYNAMIC_ITEMS)
			return CompletableFuture.completedFuture(null);
		
		startUncachedProcessingAll();
		CompletableFuture<Void> future = new CompletableFuture<>();
		Thread thread = new Thread(() -> {
			lock.lockAll();
			try {
				Exception toThrow = new Exception("Error unloading page(s)");
				for (File file : CLIENT_CHEST_FOLDER.listFiles()) {
					if (!file.getName().matches("page[0-9]+\\.nbt"))
						continue;
					int page = Integer.parseInt(file.getName().substring("page".length(), file.getName().indexOf('.')));
					if (page >= cache.getPageCount())
						continue;
					if (getDataVersionStatus(page).map(status -> status == DataVersionStatus.CURRENT).orElse(false)) {
						try {
							unloadPageSync(page, loadLevel);
						} catch (Throwable e) {
							toThrow.addSuppressed(new Exception("Page " + (page + 1), e));
						}
					}
				}
				if (toThrow.getSuppressed().length > 0) {
					NBTEditor.LOGGER.error("Error unloading the client chest!", toThrow);
					future.completeExceptionally(toThrow);
				} else
					future.complete(null);
			} finally {
				lock.unlockAll();
				finishUncachedProcessingAll();
			}
		}, "NBTEditor/Async/ClientChest/Unloading");
		thread.start();
		return future;
	}
	
	public CompletableFuture<ClientChestPage> unloadPage(int page, PageLoadLevel loadLevel) {
		if (!isUncachedProcessingPage(page)) {
			ClientChestPage cachedPage = cache.getCachedPage(page);
			if (cachedPage.loadLevel().ordinal() <= loadLevel.ordinal() || !cachedPage.isInThisVersion())
				return CompletableFuture.completedFuture(cachedPage);
		}
		
		startUncachedProcessing(page);
		CompletableFuture<ClientChestPage> future = new CompletableFuture<>();
		Thread thread = new Thread(() -> {
			lock.lock(page);
			try {
				future.complete(unloadPageSync(page, loadLevel));
			} catch (Throwable e) {
				NBTEditor.LOGGER.error("Error unloading client chest page " + (page + 1), e);
				future.completeExceptionally(e);
			} finally {
				lock.unlock(page);
				finishUncachedProcessing(page);
			}
		}, "NBTEditor/Async/ClientChest/Unloading/" + page);
		thread.start();
		return future;
	}
	
	public CompletableFuture<ClientChestPage> reloadPage(int page) {
		PageLoadLevel loadLevel = getLoadLevel(page);
		cache.discardPageCache(page);
		return getPage(page, loadLevel);
	}
	
	public CompletableFuture<Void> importAllPages() {
		if (!CLIENT_CHEST_FOLDER.exists())
			return CompletableFuture.completedFuture(null);
		
		startUncachedProcessingAll();
		CompletableFuture<Void> future = new CompletableFuture<>();
		Thread thread = new Thread(() -> {
			lock.lockAll();
			try {
				Exception toThrow = new Exception("Error importing page(s)");
				for (File file : CLIENT_CHEST_FOLDER.listFiles()) {
					if (!file.getName().matches("page[0-9]+\\.nbt"))
						continue;
					int page = Integer.parseInt(file.getName().substring("page".length(), file.getName().indexOf('.')));
					if (page >= cache.getPageCount())
						continue;
					if (getDataVersionStatus(page).map(status -> status == DataVersionStatus.UNKNOWN).orElse(true)) {
						try {
							importPageSync(page, true);
						} catch (Throwable e) {
							toThrow.addSuppressed(new Exception("Page " + (page + 1), e));
						}
					}
				}
				if (toThrow.getSuppressed().length > 0) {
					NBTEditor.LOGGER.error("Error importing the client chest!", toThrow);
					future.completeExceptionally(toThrow);
				} else
					future.complete(null);
			} finally {
				lock.unlockAll();
				finishUncachedProcessingAll();
			}
		}, "NBTEditor/Async/ClientChest/Importing");
		thread.start();
		return future;
	}
	public CompletableFuture<Void> importPage(int page) {
		File file = getFile(page);
		if (!file.exists())
			throw new IllegalStateException("Cannot import an up to date page!");
		
		if (getDataVersionStatus(page).map(status -> status != DataVersionStatus.UNKNOWN).orElse(false))
			throw new IllegalStateException("Cannot import a page with a DataVersion tag!");
		
		startUncachedProcessing(page);
		CompletableFuture<Void> future = new CompletableFuture<>();
		Thread thread = new Thread(() -> {
			lock.lock(page);
			try {
				importPageSync(page, false);
				future.complete(null);
			} catch (Throwable e) {
				NBTEditor.LOGGER.error("Error importing client chest page " + (page + 1), e);
				future.completeExceptionally(e);
			} finally {
				lock.unlock(page);
				finishUncachedProcessing(page);
			}
		}, "NBTEditor/Async/ClientChest/Importing/" + page);
		thread.start();
		return future;
	}
	
	public CompletableFuture<Void> updateAllPages(Optional<Integer> defaultDataVersion) {
		if (!CLIENT_CHEST_FOLDER.exists())
			return CompletableFuture.completedFuture(null);
		
		startUncachedProcessingAll();
		CompletableFuture<Void> future = new CompletableFuture<>();
		Thread thread = new Thread(() -> {
			lock.lockAll();
			try {
				Exception toThrow = new Exception("Error updating page(s)");
				for (File file : CLIENT_CHEST_FOLDER.listFiles()) {
					if (!file.getName().matches("page[0-9]+\\.nbt"))
						continue;
					int page = Integer.parseInt(file.getName().substring("page".length(), file.getName().indexOf('.')));
					if (page >= cache.getPageCount())
						continue;
					if (getDataVersionStatus(page).map(status -> status.canBeUpdated(defaultDataVersion.isPresent())).orElse(true)) {
						try {
							boolean unloaded = (getLoadLevel(page) == PageLoadLevel.UNLOADED);
							if (updatePageSync(page, defaultDataVersion, true) != null) {
								if (unloaded)
									cache.discardPageCache(page);
							}
						} catch (Throwable e) {
							toThrow.addSuppressed(new Exception("Page " + (page + 1), e));
						}
					}
				}
				if (toThrow.getSuppressed().length > 0) {
					NBTEditor.LOGGER.error("Error updating the client chest!", toThrow);
					future.completeExceptionally(toThrow);
				} else
					future.complete(null);
			} finally {
				lock.unlockAll();
				finishUncachedProcessingAll();
			}
		}, "NBTEditor/Async/ClientChest/Updating");
		thread.start();
		return future;
	}
	public CompletableFuture<ClientChestPage> updatePage(int page, Optional<Integer> defaultDataVersion) {
		File file = new File(CLIENT_CHEST_FOLDER, "page" + page + ".nbt");
		if (!file.exists())
			throw new IllegalStateException("Cannot update an already up to date page!");
		
		getDataVersion(page).ifPresent(fileDataVersion -> {
			int dataVersion = fileDataVersion.or(() -> defaultDataVersion).orElseThrow(
					() -> new IllegalStateException("Missing DataVersion tag and default DataVersion!"));
			if (dataVersion == Version.getDataVersion())
				throw new IllegalStateException("Cannot update an already up to date page!");
			if (dataVersion > Version.getDataVersion())
				throw new IllegalStateException("Cannot downgrade pages!");
		});
		
		startUncachedProcessing(page);
		CompletableFuture<ClientChestPage> future = new CompletableFuture<>();
		Thread thread = new Thread(() -> {
			lock.lock(page);
			try {
				future.complete(updatePageSync(page, defaultDataVersion, false));
			} catch (Throwable e) {
				NBTEditor.LOGGER.error("Error updating client chest page " + (page + 1), e);
				future.completeExceptionally(e);
			} finally {
				lock.unlock(page);
				finishUncachedProcessing(page);
			}
		}, "NBTEditor/Async/ClientChest/Updating/" + page);
		thread.start();
		return future;
	}
	
	public CompletableFuture<Void> discardPage(int page) {
		File file = new File(CLIENT_CHEST_FOLDER, "page" + page + ".nbt");
		if (!file.exists())
			throw new IllegalStateException("Cannot discard an up to date page!");
		
		if (getDataVersionStatus(page).map(status -> status == DataVersionStatus.CURRENT).orElse(false))
			throw new IllegalStateException("Cannot discard an up to date page!");
		
		startUncachedProcessing(page);
		CompletableFuture<Void> future = new CompletableFuture<>();
		Thread thread = new Thread(() -> {
			lock.lock(page);
			try {
				discardPageSync(page);
				future.complete(null);
			} catch (Throwable e) {
				NBTEditor.LOGGER.error("Error discarding client chest page " + (page + 1), e);
				future.completeExceptionally(e);
			} finally {
				lock.unlock(page);
				finishUncachedProcessing(page);
			}
		}, "NBTEditor/Async/ClientChest/Discarding/" + page);
		thread.start();
		return future;
	}
	
	public int[] getNearestPOIs(int page) {
		int pageCount = getPageCount();
		int[] output = cache.getNearestItems(page);
		if (output[1] >= pageCount)
			output[1] = -1;
		
		for (int namedPage : pageToName.keySet()) {
			if (namedPage >= pageCount)
				continue;
			if (namedPage < page && namedPage > output[0])
				output[0] = namedPage;
			if (namedPage > page && (namedPage < output[1] || output[1] == -1))
				output[1] = namedPage;
		}
		return output;
	}
	
	
	private void startUncachedProcessing(int page) {
		uncachedProcessers.compute(page, (key, value) -> (value == null ? 0 : value) + 1);
	}
	private void finishUncachedProcessing(int page) {
		uncachedProcessers.compute(page, (key, value) -> value == 1 ? null : value - 1);
	}
	private void startUncachedProcessingAll() {
		startUncachedProcessing(-1);
	}
	private void finishUncachedProcessingAll() {
		finishUncachedProcessing(-1);
	}
	
	private File getFile(int page) {
		return new File(CLIENT_CHEST_FOLDER, "page" + page + ".nbt");
	}
	
	private Optional<Optional<Integer>> getDataVersion(int page) {
		ClientChestPage pageData = cache.getCachedPage(page);
		if (pageData.loadLevel() == PageLoadLevel.UNLOADED)
			return Optional.empty();
		return Optional.of(pageData.dataVersion());
	}
	private Optional<DataVersionStatus> getDataVersionStatus(int page) {
		return getDataVersion(page).map(DataVersionStatus::of);
	}
	private Optional<Integer> readDataVersionSync(int page) throws Exception {
		File file = getFile(page);
		if (!file.exists())
			return Optional.of(Version.getDataVersion());
		
		NbtCompound pageNbt = MVMisc.readNbt(file);
		if (!pageNbt.contains("DataVersion", NbtElement.NUMBER_TYPE))
			return Optional.empty();
		return Optional.of(pageNbt.getInt("DataVersion"));
	}
	private DataVersionStatus readDataVersionStatusSync(int page) throws Exception {
		return DataVersionStatus.of(readDataVersionSync(page));
	}
	
	private ClientChestPage readPageSync(int page, PageLoadLevel loadLevel) throws Throwable {
		ClientChestPage cachedPage = cache.getCachedPage(page);
		if (loadLevel.ordinal() <= cachedPage.loadLevel().ordinal())
			return cachedPage;
		if (loadLevel == PageLoadLevel.DYNAMIC_ITEMS && cachedPage.loadLevel() == PageLoadLevel.NORMAL_ITEMS) {
			ItemStack[] items = Arrays.copyOf(cachedPage.items(), 54);
			DynamicItems dynamicItems = cachedPage.dynamicItems().copy();
			
			for (int slot : dynamicItems.getSlots())
				items[slot] = dynamicItems.tryLoad(slot);
			
			ClientChestPage pageData = new ClientChestPage(items, dynamicItems, PageLoadLevel.DYNAMIC_ITEMS);
			cache.cachePage(page, pageData);
			return pageData;
		}
		
		File file = getFile(page);
		if (!file.exists()) {
			cache.cacheEmptyPage(page);
			return new ClientChestPage();
		}
		
		NbtCompound pageNbt = MVMisc.readNbt(file);
		if (!pageNbt.contains("DataVersion", NbtElement.NUMBER_TYPE)) {
			ClientChestPage output = ClientChestPage.unknownDataVersion();
			cache.cachePage(page, output);
			return output;
		}
		int dataVersion = pageNbt.getInt("DataVersion");
		if (dataVersion != Version.getDataVersion()) {
			ClientChestPage output = ClientChestPage.wrongDataVersion(dataVersion);
			cache.cachePage(page, output);
			return output;
		}
		
		NbtList itemsNbt = pageNbt.getList("items", NbtElement.COMPOUND_TYPE);
		ItemStack[] items = new ItemStack[54];
		DynamicItems dynamicItems = new DynamicItems();
		boolean empty = true;
		int i = -1;
		for (NbtElement itemElementNbt : itemsNbt) {
			i++;
			NbtCompound itemNbt = (NbtCompound) itemElementNbt;
			if (itemNbt.contains("dynamic", NbtElement.BYTE_TYPE) && itemNbt.getBoolean("dynamic")) {
				itemNbt.remove("dynamic");
				dynamicItems.add(i, itemNbt, false);
				empty = false;
			} else {
				items[i] = MVMisc.withDefaultRegistryManager(() -> NBTManagers.ITEM.deserialize(itemNbt, true));
				if (empty && items[i] != null && !items[i].isEmpty())
					empty = false;
			}
		}
		if (empty) {
			cache.cacheEmptyPage(page);
			Files.delete(file.toPath());
			return new ClientChestPage();
		} else {
			if (loadLevel == PageLoadLevel.DYNAMIC_ITEMS) {
				for (int slot : dynamicItems.getSlots())
					items[slot] = dynamicItems.tryLoad(slot);
			}
			ClientChestPage output = new ClientChestPage(items, dynamicItems, loadLevel);
			cache.cachePage(page, output);
			return output;
		}
	}
	
	private void writePageSync(int page, ClientChestPage pageData) throws Throwable {
		if (pageData.loadLevel() == PageLoadLevel.UNLOADED)
			return;
		
		Optional<DataVersionStatus> dataVersionStatus = getDataVersionStatus(page);
		if (dataVersionStatus.isEmpty())
			dataVersionStatus = Optional.of(readDataVersionStatusSync(page));
		if (dataVersionStatus.get() != DataVersionStatus.CURRENT)
			throw new IllegalStateException("Cannot write to a page which has a different DataVersion!");
		
		ItemStack[] items = pageData.getItemsOrThrow();
		DynamicItems dynamicItems = pageData.dynamicItems();
		
		NbtCompound pageNbt = new NbtCompound();
		pageNbt.putInt("DataVersion", Version.getDataVersion());
		
		NbtList itemsNbt = new NbtList();
		for (int i = 0; i < items.length; i++) {
			NbtCompound itemNbt;
			if (dynamicItems.isSlot(i))
				itemNbt = dynamicItems.getOriginalNbt(i);
			else
				itemNbt = (items[i] == null ? ItemStack.EMPTY : items[i]).manager$serialize(true);
			
			if (dynamicItems.isSlot(i)) {
				itemNbt = itemNbt.copy();
				itemNbt.putByte("dynamic", (byte) 1);
			}
			
			itemsNbt.add(itemNbt);
		}
		
		pageNbt.put("items", itemsNbt);
		
		if (!CLIENT_CHEST_FOLDER.exists())
			CLIENT_CHEST_FOLDER.mkdir();
		File file = getFile(page);
		File tmpFile = new File(CLIENT_CHEST_FOLDER, "saving_page" + page + "_" + System.currentTimeMillis() + ".nbt");
		MixinLink.throwHiddenException(() -> MVMisc.writeNbt(pageNbt, tmpFile));
		Files.move(tmpFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}
	
	private ClientChestPage unloadPageSync(int page, PageLoadLevel loadLevel) {
		ClientChestPage cachedPage = cache.getCachedPage(page);
		
		if (cachedPage.loadLevel().ordinal() <= loadLevel.ordinal() || !cachedPage.isInThisVersion())
			return cache.getCachedPage(page);
		
		if (loadLevel == PageLoadLevel.UNLOADED) {
			cache.discardPageCache(page);
			return ClientChestPage.unloaded();
		}
		
		DynamicItems dynamicItems = cachedPage.dynamicItems().copy();
		dynamicItems.unloadAll();
		
		cachedPage = new ClientChestPage(cachedPage.items(), dynamicItems, PageLoadLevel.NORMAL_ITEMS);
		cache.cachePage(page, cachedPage);
		return cachedPage;
	}
	
	private void importPageSync(int page, boolean ignoreInvalidDataVersion) throws Throwable {
		File file = getFile(page);
		if (!file.exists()) {
			if (ignoreInvalidDataVersion)
				return;
			throw new IllegalStateException("Cannot import an up to date page!");
		}
		
		NbtCompound pageNbt = MVMisc.readNbt(file);
		if (pageNbt.contains("DataVersion", NbtElement.NUMBER_TYPE)) {
			if (ignoreInvalidDataVersion)
				return;
			throw new IllegalStateException("Cannot import a page with a DataVersion tag!");
		}
		
		Files.copy(file.toPath(), new File(CLIENT_CHEST_FOLDER, "importing_page" + page + "_" + System.currentTimeMillis() + ".nbt").toPath());
		
		pageNbt.putInt("DataVersion", Version.getDataVersion());
		MixinLink.throwHiddenException(() -> MVMisc.writeNbt(pageNbt, file));
		
		PageLoadLevel loadLevel = getLoadLevel(page);
		cache.discardPageCache(page);
		readPageSync(page, loadLevel);
	}
	
	private ClientChestPage updatePageSync(int page, Optional<Integer> defaultDataVersion, boolean ignoreInvalidDataVersion) throws Throwable {
		File file = new File(CLIENT_CHEST_FOLDER, "page" + page + ".nbt");
		if (!file.exists()) {
			if (ignoreInvalidDataVersion)
				return null;
			throw new IllegalStateException("Cannot update an already up to date page!");
		}
		
		NbtCompound pageNbt = MVMisc.readNbt(file);
		int dataVersion;
		try {
			dataVersion = (pageNbt.contains("DataVersion", NbtElement.NUMBER_TYPE) ? pageNbt.getInt("DataVersion") :
					defaultDataVersion.orElseThrow(() -> new IllegalStateException("Missing DataVersion tag and default DataVersion!")));
			if (dataVersion == Version.getDataVersion())
				throw new IllegalStateException("Cannot update an already up to date page!");
			if (dataVersion > Version.getDataVersion())
				throw new IllegalStateException("Cannot downgrade pages!");
		} catch (IllegalStateException e) {
			if (ignoreInvalidDataVersion)
				return null;
			throw e;
		}
		
		Files.copy(file.toPath(), new File(CLIENT_CHEST_FOLDER, "updating_page" + page + "_" + System.currentTimeMillis() + ".nbt").toPath());
		
		NbtList itemsNbt = pageNbt.getList("items", NbtElement.COMPOUND_TYPE);
		ItemStack[] items = new ItemStack[54];
		DynamicItems dynamicItems = new DynamicItems();
		boolean empty = true;
		int i = -1;
		for (NbtElement itemElementNbt : itemsNbt) {
			i++;
			NbtCompound itemNbt = (NbtCompound) itemElementNbt;
			boolean dynamic = (itemNbt.contains("dynamic", NbtElement.BYTE_TYPE) && itemNbt.getBoolean("dynamic"));
			if (dynamic)
				itemNbt.remove("dynamic");
			
			itemNbt = MainUtil.updateDynamic(TypeReferences.ITEM_STACK, itemNbt, dataVersion);
			
			if (dynamic) {
				dynamicItems.add(i, itemNbt, false);
				empty = false;
			} else {
				final NbtCompound finalItemNbt = itemNbt;
				items[i] = MVMisc.withDefaultRegistryManager(() -> NBTManagers.ITEM.deserialize(finalItemNbt, true));
				if (empty && items[i] != null && !items[i].isEmpty())
					empty = false;
			}
		}
		if (empty) {
			cache.cacheEmptyPage(page);
			Files.delete(file.toPath());
			return new ClientChestPage();
		} else {
			ClientChestPage output = new ClientChestPage(items, dynamicItems, PageLoadLevel.NORMAL_ITEMS);
			cache.cachePage(page, output);
			writePageSync(page, output);
			return output;
		}
	}
	
	private void discardPageSync(int page) throws Throwable {
		File file = new File(CLIENT_CHEST_FOLDER, "page" + page + ".nbt");
		if (!file.exists())
			throw new IllegalStateException("Cannot discard an up to date page!");
		
		NbtCompound pageNbt = MVMisc.readNbt(file);
		if (pageNbt.contains("DataVersion", NbtElement.NUMBER_TYPE) && pageNbt.getInt("DataVersion") == Version.getDataVersion())
			throw new IllegalStateException("Cannot discard an up to date page!");
		
		cache.cacheEmptyPage(page);
		Files.delete(file.toPath());
	}
	
	private void backupCorruptPage(int page) {
		File file = getFile(page);
		if (!file.exists())
			return;
		file.renameTo(new File(CLIENT_CHEST_FOLDER, "corrupt_page" + page + "_" + System.currentTimeMillis() + ".nbt"));
		warnCorrupt();
	}
	public void warnIfCorrupt() {
		if (!CLIENT_CHEST_FOLDER.exists())
			return;
		try {
			if (Files.list(CLIENT_CHEST_FOLDER.toPath()).anyMatch(path -> path.toFile().getName().startsWith("corrupt_page")))
				warnCorrupt();
		} catch (Exception e) {
			NBTEditor.LOGGER.error("Error checking for corrupt pages", e);
		}
	}
	private void warnCorrupt() {
		if (MainUtil.client.player == null)
			return;
		MainUtil.client.player.sendMessage(attachShowFolder(TextInst.translatable("nbteditor.client_chest.corrupt_warning")), false);
	}
	public static Text attachShowFolder(EditableText text) {
		return text.append(" ").append(TextInst.translatable("nbteditor.file_options.show").styled(
				style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, CLIENT_CHEST_FOLDER.getAbsolutePath()))));
	}
	
}
