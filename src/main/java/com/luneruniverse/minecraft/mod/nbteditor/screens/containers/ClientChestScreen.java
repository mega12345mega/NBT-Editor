package com.luneruniverse.minecraft.mod.nbteditor.screens.containers;

import org.lwjgl.glfw.GLFW;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.clientchest.ClientChestHelper;
import com.luneruniverse.minecraft.mod.nbteditor.clientchest.ClientChestPage;
import com.luneruniverse.minecraft.mod.nbteditor.clientchest.DynamicItems;
import com.luneruniverse.minecraft.mod.nbteditor.clientchest.PageLoadLevel;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.EditableText;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVTooltip;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ClientChestItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ClientChestDataVersionScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.LoadingScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.util.FancyConfirmScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.NamedTextFieldWidget;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

public class ClientChestScreen extends ClientHandledScreen {
	
	public static int PAGE = 0;
	public static int prevPageJumpTarget;
	public static int nextPageJumpTarget;
	
	public static void show() {
		LoadingScreen.show(
				ClientChestHelper.getPage(PAGE, PageLoadLevel.DYNAMIC_ITEMS),
				NBTEditorClient.CURSOR_MANAGER::closeRoot,
				(loaded, optional) -> {
					if (optional.isEmpty()) {
						NBTEditorClient.CURSOR_MANAGER.closeRoot();
						return;
					}
					
					ClientChestPage pageData = optional.get();
					
					if (!pageData.isInThisVersion()) {
						NBTEditorClient.CURSOR_MANAGER.closeRoot();
						MainUtil.client.setScreen(new ClientChestDataVersionScreen(pageData.dataVersion()));
						return;
					}
					
					if (MainUtil.client.currentScreen instanceof ClientChestScreen screen) {
						screen.setPageData(pageData);
						MainUtil.setTextFieldValueSilently(screen.pageField, (PAGE + 1) + "", true);
						screen.updatePageNavigation();
					} else {
						ClientChestScreen screen = new ClientChestScreen();
						screen.setPageData(pageData);
						NBTEditorClient.CURSOR_MANAGER.showBranch(screen);
						NBTEditorClient.CLIENT_CHEST.warnIfCorrupt();
					}
				});
	}
	
	private DynamicItems dynamicItems;
	private boolean navigationClicked;
	private NamedTextFieldWidget nameField;
	private ButtonWidget prevPage;
	private TextFieldWidget pageField;
	private ButtonWidget nextPage;
	private ButtonWidget prevPageJump;
	private ButtonWidget nextPageJump;
	
	private ClientChestScreen() {
		super(new ClientScreenHandler(6), TextInst.translatable("nbteditor.client_chest"));
	}
	private void setPageData(ClientChestPage pageData) {
		ItemStack[] items = pageData.getItemsOrThrow();
		for (int i = 0; i < items.length; i++)
			handler.getSlot(i).setStackNoCallbacks(items[i] == null ? ItemStack.EMPTY : items[i].copy());
		dynamicItems = pageData.dynamicItems();
	}
	
	@Override
	protected void init() {
		this.clearChildren();
		super.init();
		x += 87 / 2;
		
		nameField = new NamedTextFieldWidget(this.x - 87, this.y, 83, 16) {
			@Override
			public boolean mouseClicked(double mouseX, double mouseY, int button) {
				boolean output = super.mouseClicked(mouseX, mouseY, button);
				if (output)
					navigationClicked = true;
				return output;
			}
			@Override
			public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
				if (keyCode == GLFW.GLFW_KEY_ENTER && !nameField.isValid()) {
					nameField.setValid(true);
					ClientChestHelper.setNameOfPage(PAGE, nameField.getText());
					return true;
				}
				return super.keyPressed(keyCode, scanCode, modifiers);
			}
		}.name(TextInst.translatable("nbteditor.client_chest.page_name"));
		nameField.setMaxLength(Integer.MAX_VALUE);
		nameField.setChangedListener(name -> {
			if (NBTEditorClient.CLIENT_CHEST.isNameUsedByOther(name, PAGE)) {
				nameField.setValid(false);
				return;
			}
			nameField.setValid(true);
			ClientChestHelper.setNameOfPage(PAGE, name);
		});
		this.addDrawableChild(nameField);
		
		pageField = new TextFieldWidget(textRenderer, this.x - 63, this.y + 22, 35, 16, TextInst.of("")) {
			@Override
			public boolean mouseClicked(double mouseX, double mouseY, int button) {
				boolean output = super.mouseClicked(mouseX, mouseY, button);
				if (output)
					navigationClicked = true;
				return output;
			}
		};
		pageField.setMaxLength((NBTEditorClient.CLIENT_CHEST.getPageCount() + "").length());
		pageField.setText((PAGE + 1) + "");
		pageField.setChangedListener(str -> {
			if (str.isEmpty() || str.equals("+") || str.equals("-"))
				return;
			
			int intVal = Integer.parseInt(str);
			if (intVal > 0) {
				PAGE = intVal - 1;
				show();
			}
		});
		pageField.setTextPredicate(MainUtil.intPredicate(() -> 0, NBTEditorClient.CLIENT_CHEST::getPageCount, true));
		this.addDrawableChild(pageField);
		
		EditableText prevKeybind = TextInst.translatable("nbteditor.keybind.page.down");
		EditableText nextKeybind = TextInst.translatable("nbteditor.keybind.page.up");
		if (ConfigScreen.isInvertedPageKeybinds()) {
			EditableText temp = prevKeybind;
			prevKeybind = nextKeybind;
			nextKeybind = temp;
		}
		
		this.addDrawableChild(prevPage = MVMisc.newButton(this.x - 87, this.y + 20, 20, 20, TextInst.of("<"), btn -> {
			navigationClicked = true;
			prevPage();
		}, ConfigScreen.isKeybindsHidden() ? null : new MVTooltip(TextInst.literal("")
				.append(prevKeybind).append(TextInst.translatable("nbteditor.keybind.page.prev")))));
		
		this.addDrawableChild(nextPage = MVMisc.newButton(this.x - 24, this.y + 20, 20, 20, TextInst.of(">"), btn -> {
			navigationClicked = true;
			nextPage();
		}, ConfigScreen.isKeybindsHidden() ? null : new MVTooltip(TextInst.literal("")
				.append(nextKeybind).append(TextInst.translatable("nbteditor.keybind.page.next")))));
		
		this.addDrawableChild(prevPageJump = MVMisc.newButton(this.x - 87, this.y + 44, 39, 20, TextInst.of("<<"), btn -> {
			navigationClicked = true;
			prevPageJump();
		}, ConfigScreen.isKeybindsHidden() ? null : new MVTooltip(TextInst.translatable("nbteditor.keybind.page.shift")
				.append(prevKeybind).append(TextInst.translatable("nbteditor.keybind.page.prev_jump")))));
		
		this.addDrawableChild(nextPageJump = MVMisc.newButton(this.x - 43, this.y + 44, 39, 20, TextInst.of(">>"), btn -> {
			navigationClicked = true;
			nextPageJump();
		}, ConfigScreen.isKeybindsHidden() ? null : new MVTooltip(TextInst.translatable("nbteditor.keybind.page.shift")
				.append(nextKeybind).append(TextInst.translatable("nbteditor.keybind.page.next_jump")))));
		
		this.addDrawableChild(MVMisc.newButton(this.x - 87, this.y + 68, 83, 20, ConfigScreen.isLockSlots() ? TextInst.translatable("nbteditor.client_chest.slots.unlock") : TextInst.translatable("nbteditor.client_chest.slots.lock"), btn -> {
			navigationClicked = true;
			if (ConfigScreen.isLockSlotsRequired()) {
				btn.active = false;
				ConfigScreen.setLockSlots(true);
			} else
				ConfigScreen.setLockSlots(!ConfigScreen.isLockSlots());
			btn.setMessage(ConfigScreen.isLockSlots() ? TextInst.translatable("nbteditor.client_chest.slots.unlock") : TextInst.translatable("nbteditor.client_chest.slots.lock"));
		})).active = !ConfigScreen.isLockSlotsRequired();
		
		this.addDrawableChild(MVMisc.newButton(this.x - 87, this.y + 92, 83, 20, TextInst.translatable("nbteditor.client_chest.reload_page"), btn -> {
			navigationClicked = true;
			LoadingScreen.show(ClientChestHelper.reloadPage(PAGE), this::close, (loaded, pageData) -> show());
		}));
		
		this.addDrawableChild(MVMisc.newButton(this.x - 87, this.y + 116, 83, 20, TextInst.translatable("nbteditor.client_chest.clear_page"), btn -> {
			navigationClicked = true;
			client.setScreen(new FancyConfirmScreen(value -> {
				if (value) {
					this.handler.getInventory().clear();
					save();
				}
				
				client.setScreen(ClientChestScreen.this);
			}, TextInst.translatable("nbteditor.client_chest.clear_page.title"), TextInst.translatable("nbteditor.client_chest.clear_page.desc"),
					TextInst.translatable("nbteditor.client_chest.clear_page.yes"), TextInst.translatable("nbteditor.client_chest.clear_page.no")));
		}));
		
		
		updatePageNavigation();
	}
	public void updatePageNavigation() {
		nameField.setText(NBTEditorClient.CLIENT_CHEST.getNameFromPage(PAGE));
		
		int[] jumps = NBTEditorClient.CLIENT_CHEST.getNearestPOIs(PAGE);
		int maxPage = NBTEditorClient.CLIENT_CHEST.getPageCount() - 1;
		prevPageJumpTarget = jumps[0] == -1 ? (PAGE == 0 ? -1 : 0) : jumps[0];
		nextPageJumpTarget = jumps[1] == -1 ? (PAGE == maxPage ? -1 : maxPage) : jumps[1];
		
		prevPage.active = PAGE != 0;
		nextPage.active = PAGE != maxPage;
		prevPageJump.active = prevPageJumpTarget != -1;
		nextPageJump.active = nextPageJumpTarget != -1;
	}
	
	@Override
	protected void handledScreenTick() {
		nameField.tick();
		pageField.tick();
	}
	
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		navigationClicked = false;
		
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			close();
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_PAGE_UP || keyCode == GLFW.GLFW_KEY_PAGE_DOWN) {
			boolean prev = (keyCode == GLFW.GLFW_KEY_PAGE_DOWN);
			if (ConfigScreen.isInvertedPageKeybinds())
				prev = !prev;
			boolean jump = hasShiftDown();
			if (prev) {
				if (jump)
					prevPageJump();
				else
					prevPage();
			} else {
				if (jump)
					nextPageJump();
				else
					nextPage();
			}
			return true;
		}
		
		if (focusedSlot != null) {
			boolean lockedSlot = (focusedSlot.inventory == handler.getInventory() &&
					dynamicItems.isSlotLocked(focusedSlot.getIndex()));
			if (!lockedSlot || keyCode == GLFW.GLFW_KEY_DELETE) {
				if (handleKeybind(keyCode, focusedSlot, ClientChestScreen::show, slot -> new ClientChestItemReference(PAGE, slot.getIndex()))) {
					if (keyCode == GLFW.GLFW_KEY_DELETE && lockedSlot)
						dynamicItems.remove(focusedSlot.getIndex());
					return true;
				}
			}
		}
		
		return !this.nameField.keyPressed(keyCode, scanCode, modifiers) && !this.nameField.isActive() &&
				!this.pageField.keyPressed(keyCode, scanCode, modifiers) && !this.pageField.isActive()
				? super.keyPressed(keyCode, scanCode, modifiers) : true;
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		navigationClicked = false;
		MVMisc.setKeyboardRepeatEvents(this.nameField.mouseClicked(mouseX, mouseY, button) ||
				this.pageField.mouseClicked(mouseX, mouseY, button));
		super.mouseClicked(mouseX, mouseY, button);
		return true;
	}
	
	@Override
	protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
		if (navigationClicked)
			return;
		
		super.onMouseClick(slot, slotId, button, actionType);
	}
	@Override
	public boolean allowEnchantmentCombine() {
		return true;
	}
	@Override
	public LockedSlotsInfo getLockedSlotsInfo() {
		LockedSlotsInfo info = (ConfigScreen.isLockSlots() ? LockedSlotsInfo.ITEMS_LOCKED : LockedSlotsInfo.NONE).copy();
		
		for (int slot : dynamicItems.getLockedSlots())
			info.addContainerSlot(slot);
		
		return info;
	}
	@Override
	public void onChange() {
		save();
	}
	
	private void save() {
		ItemStack[] items = new ItemStack[54];
		for (int i = 0; i < this.handler.getInventory().size(); i++)
			items[i] = this.handler.getInventory().getStack(i).copy();
		
		ClientChestHelper.setPage(PAGE, items, dynamicItems);
	}
	
	@Override
	protected Text getRenderedTitle() {
		EditableText title = TextInst.copy(this.title).append(" (" + (PAGE + 1) + ")");
		return NBTEditorClient.CLIENT_CHEST.isProcessingPage(PAGE) ? title.append("*") : title;
	}
	
	@Override
	public boolean shouldPause() {
		return true;
	}
	
	@Override
	public void removed() {
		MVMisc.setKeyboardRepeatEvents(false);
	}
	
	private void prevPage() {
		if (!prevPage.active)
			return;
		PAGE--;
		show();
	}
	private void nextPage() {
		if (!nextPage.active)
			return;
		PAGE++;
		show();
	}
	
	private void prevPageJump() {
		if (!prevPageJump.active)
			return;
		PAGE = prevPageJumpTarget;
		show();
	}
	private void nextPageJump() {
		if (!nextPageJump.active)
			return;
		PAGE = nextPageJumpTarget;
		show();
	}
	
}
