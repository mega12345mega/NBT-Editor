package com.luneruniverse.minecraft.mod.nbteditor.clientchest;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.EditableText;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.NBTManagers;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.datafixer.TypeReferences;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;

public abstract class ClientChest {
	
	public static final File CLIENT_CHEST_FOLDER = new File(NBTEditorClient.SETTINGS_FOLDER, "client_chest");
	private static final File PAGE_NAMES = new File(CLIENT_CHEST_FOLDER, "page_names.json");
	
	private final Map<String, Integer> nameToPage;
	private final Map<Integer, String> pageToName;
	
	@SuppressWarnings("serial")
	public ClientChest() {
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
	}
	
	public Integer getPageFromName(String name) {
		Integer page = nameToPage.get(name);
		if (page != null && page >= getPageCount())
			return null;
		return page;
	}
	public String getNameFromPage(int page) {
		if (page >= getPageCount())
			return "";
		return pageToName.getOrDefault(page, "");
	}
	public Set<String> getAllPageNames() {
		return nameToPage.entrySet().stream().filter(entry -> entry.getValue() < getPageCount())
				.map(Map.Entry::getKey).collect(Collectors.toUnmodifiableSet());
	}
	public boolean isNameUsedByOther(String name, int page) {
		Integer matchingPage = getPageFromName(name);
		return matchingPage != null && matchingPage != page;
	}
	@SuppressWarnings("serial")
	public void setNameOfPage(int page, String name) throws IOException {
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
	
	public void loadAync() {
		Thread loader = new Thread(() -> {
			try {
				loadSync();
			} catch (Exception e) {
				NBTEditor.LOGGER.error("Unable to load the client chest!", e);
			}
		}, "NBTEditor/Async/ClientChestLoader");
		loader.setDaemon(true);
		loader.start();
	}
	protected abstract void loadSync() throws Exception;
	protected ClientChestPage loadSync(int page) throws Exception {
		File file = new File(CLIENT_CHEST_FOLDER, "page" + page + ".nbt");
		if (!file.exists()) {
			cacheEmptyPage(page);
			return new ClientChestPage();
		}
		
		NbtCompound pageNbt = MVMisc.readNbt(file);
		if (!pageNbt.contains("DataVersion", NbtElement.NUMBER_TYPE)) {
			ClientChestPage output = ClientChestPage.unknownDataVersion();
			cachePage(page, output);
			return output;
		}
		int dataVersion = pageNbt.getInt("DataVersion");
		if (dataVersion != Version.getDataVersion()) {
			ClientChestPage output = ClientChestPage.wrongDataVersion(dataVersion);
			cachePage(page, output);
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
				items[i] = NBTManagers.ITEM.deserialize(itemNbt, true);
				if (empty && items[i] != null && !items[i].isEmpty())
					empty = false;
			}
		}
		if (empty) {
			cacheEmptyPage(page);
			file.delete();
			return new ClientChestPage();
		} else {
			ClientChestPage output = new ClientChestPage(items, dynamicItems);
			output.tryLoadDynamicItems();
			cachePage(page, output);
			return output;
		}
	}
	
	public void tryLoadDynamicItemsAsync() {
		Thread loader = new Thread(() -> {
			try {
				tryLoadDynamicItemsSync();
			} catch (Exception e) {
				NBTEditor.LOGGER.error("Unable to load dynamic items in the client chest!", e);
			}
		}, "NBTEditor/Async/ClientChestLoader");
		loader.setDaemon(true);
		loader.start();
	}
	protected abstract void tryLoadDynamicItemsSync();
	
	public ClientChestPage reloadPage(int page) {
		try {
			discardPageCache(page);
		} catch (Exception e) {
			NBTEditor.LOGGER.error("Error while reloading page #" + (page + 1), e);
		}
		return getPage(page);
	}
	
	protected void backupCorruptPage(int page) {
		File file = new File(CLIENT_CHEST_FOLDER, "page" + page + ".nbt");
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
		} catch (IOException e) {
			NBTEditor.LOGGER.error("Error checking for corrupt pages", e);
		}
	}
	private void warnCorrupt() {
		if (MainUtil.client.player == null)
			return;
		MainUtil.client.player.sendMessage(attachShowFolder(TextInst.translatable("nbteditor.client_chest.corrupt_warning")), false);
	}
	public Text attachShowFolder(EditableText text) {
		return text.append(" ").append(TextInst.translatable("nbteditor.file_options.show").styled(
				style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, CLIENT_CHEST_FOLDER.getAbsolutePath()))));
	}
	
	public abstract boolean isLoaded();
	protected void checkLoaded() {
		if (!isLoaded())
			throw new IllegalStateException("The client chest isn't loaded yet!");
	}
	
	public abstract int getPageCount();
	public abstract ClientChestPage getPage(int page);
	
	public void setPage(int page, ItemStack[] items, DynamicItems prevDynamicItems) throws Exception {
		// Just in-case there is a bug, this prevents loosing items (the items
		// array is always empty when loaded from a different DataVersion)
		if (!getPage(page).isInThisVersion())
			throw new IllegalStateException("Cannot write to a page which has a different DataVersion!");
		
		if (!CLIENT_CHEST_FOLDER.exists())
			CLIENT_CHEST_FOLDER.mkdir();
		File file = new File(CLIENT_CHEST_FOLDER, "page" + page + ".nbt");
		
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
			cacheEmptyPage(page);
			Files.deleteIfExists(file.toPath());
			return;
		}
		
		DynamicItems dynamicItems = new DynamicItems();
		for (int i = 0; i < items.length; i++) {
			if (prevDynamicItems.isSlotLocked(i))
				dynamicItems.add(i, prevDynamicItems.getOriginalNbt(i), false);
			else if (items[i] != null)
				dynamicItems.tryAdd(i, items[i]);
		}
		
		ClientChestPage pageData = new ClientChestPage(items, dynamicItems);
		cachePage(page, pageData);
		writePage(file, pageData);
	}
	
	public void importPage(int page) throws Exception {
		File file = new File(CLIENT_CHEST_FOLDER, "page" + page + ".nbt");
		if (!file.exists())
			throw new IllegalStateException("Cannot import an up to date page!");
		
		NbtCompound pageNbt = MVMisc.readNbt(file);
		if (pageNbt.contains("DataVersion", NbtElement.NUMBER_TYPE))
			throw new IllegalStateException("Cannot import a page with a DataVersion tag!");
		
		Files.copy(file.toPath(), new File(CLIENT_CHEST_FOLDER, "importing_page" + page + "_" + System.currentTimeMillis() + ".nbt").toPath());
		
		pageNbt.putInt("DataVersion", Version.getDataVersion());
		try {
			MixinLink.throwHiddenException(() -> MVMisc.writeNbt(pageNbt, file));
		} catch (Throwable e) {
			throw new IOException("Error saving client chest page", e);
		}
		
		discardPageCache(page);
	}
	public void importAllPages() throws Exception {
		if (!CLIENT_CHEST_FOLDER.exists())
			return;
		
		Exception toThrow = new Exception("Error updating page(s)");
		for (File file : CLIENT_CHEST_FOLDER.listFiles()) {
			if (!file.getName().matches("page[0-9]+\\.nbt"))
				continue;
			int page = Integer.parseInt(file.getName().substring("page".length(), file.getName().indexOf('.')));
			if (page >= getPageCount())
				continue;
			if (getPage(page).dataVersion().isEmpty()) {
				try {
					importPage(page);
				} catch (Exception e) {
					toThrow.addSuppressed(new Exception("Page " + (page + 1), e));
				}
			}
		}
		if (toThrow.getSuppressed().length > 0)
			throw toThrow;
	}
	
	public ClientChestPage updatePage(int page, Optional<Integer> defaultDataVersion) throws Exception {
		File file = new File(CLIENT_CHEST_FOLDER, "page" + page + ".nbt");
		if (!file.exists())
			throw new IllegalStateException("Cannot update an already up to date page!");
		
		NbtCompound pageNbt = MVMisc.readNbt(file);
		int dataVersion = (pageNbt.contains("DataVersion", NbtElement.NUMBER_TYPE) ? pageNbt.getInt("DataVersion") : defaultDataVersion.orElseThrow());
		if (dataVersion == Version.getDataVersion())
			throw new IllegalStateException("Cannot update an already up to date page!");
		if (dataVersion > Version.getDataVersion())
			throw new IllegalStateException("Cannot downgrade pages!");
		
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
				items[i] = NBTManagers.ITEM.deserialize(itemNbt, true);
				if (empty && items[i] != null && !items[i].isEmpty())
					empty = false;
			}
		}
		if (empty) {
			cacheEmptyPage(page);
			file.delete();
			return new ClientChestPage();
		} else {
			ClientChestPage output = new ClientChestPage(items, dynamicItems);
			output.tryLoadDynamicItems();
			cachePage(page, output);
			writePage(file, output);
			return output;
		}
	}
	public void updateAllPages(Optional<Integer> defaultDataVersion) throws Exception {
		if (!CLIENT_CHEST_FOLDER.exists())
			return;
		
		Exception toThrow = new Exception("Error updating page(s)");
		for (File file : CLIENT_CHEST_FOLDER.listFiles()) {
			if (!file.getName().matches("page[0-9]+\\.nbt"))
				continue;
			int page = Integer.parseInt(file.getName().substring("page".length(), file.getName().indexOf('.')));
			if (page >= getPageCount())
				continue;
			if (getPage(page).getDataVersionStatus().canBeUpdated(defaultDataVersion.isPresent())) {
				try {
					boolean cached = isPageCached(page);
					updatePage(page, defaultDataVersion);
					if (!cached)
						discardPageCache(page);
				} catch (Exception e) {
					toThrow.addSuppressed(new Exception("Page " + (page + 1), e));
				}
			}
		}
		if (toThrow.getSuppressed().length > 0)
			throw toThrow;
	}
	
	public void discardPage(int page) throws IOException {
		File file = new File(CLIENT_CHEST_FOLDER, "page" + page + ".nbt");
		if (!file.exists())
			throw new IllegalStateException("Cannot discard an up to date page!");
		
		NbtCompound pageNbt = MVMisc.readNbt(file);
		if (pageNbt.contains("DataVersion", NbtElement.NUMBER_TYPE) && pageNbt.getInt("DataVersion") == Version.getDataVersion())
			throw new IllegalStateException("Cannot discard an up to date page!");
		
		cacheEmptyPage(page);
		file.delete();
	}
	
	private void writePage(File file, ClientChestPage page) throws IOException {
		ItemStack[] items = page.getItemsOrThrow();
		DynamicItems dynamicItems = page.dynamicItems();
		
		NbtCompound pageNbt = new NbtCompound();
		pageNbt.putInt("DataVersion", Version.getDataVersion());
		
		NbtList itemsNbt = new NbtList();
		for (int i = 0; i < items.length; i++) {
			NbtCompound itemNbt;
			if (dynamicItems.isSlotLocked(i))
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
		
		try {
			MixinLink.throwHiddenException(() -> MVMisc.writeNbt(pageNbt, file));
		} catch (Throwable e) {
			throw new IOException("Error saving client chest page", e);
		}
	}
	
	protected abstract boolean isPageCached(int page);
	protected abstract void cachePage(int page, ClientChestPage items);
	protected abstract void cacheEmptyPage(int page);
	protected abstract void discardPageCache(int page) throws Exception;
	
	public int[] getNearestPOIs(int page) {
		int[] output = getNearestItems(page);
		for (int namedPage : pageToName.keySet()) {
			if (namedPage >= getPageCount())
				continue;
			if (namedPage < page && namedPage > output[0])
				output[0] = namedPage;
			if (namedPage > page && (namedPage < output[1] || output[1] == -1))
				output[1] = namedPage;
		}
		return output;
	}
	protected abstract int[] getNearestItems(int page);
	
}
