package com.luneruniverse.minecraft.mod.nbteditor.screens.containers;

import java.io.IOException;
import java.util.Optional;

import org.lwjgl.glfw.GLFW;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.clientchest.ClientChestPage;
import com.luneruniverse.minecraft.mod.nbteditor.clientchest.DynamicItems;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.EditableText;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVTooltip;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ClientChestItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.HandledScreenItemReference.HandledScreenItemReferenceParent;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ClientChestDataVersionScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.util.FancyConfirmScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.NamedTextFieldWidget;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.luneruniverse.minecraft.mod.nbteditor.util.SaveQueue;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

public class ClientChestScreen extends ClientHandledScreen {
	
	public static int PAGE = 0;
	public static int prevPageJumpTarget;
	public static int nextPageJumpTarget;
	
	public static void show(Optional<ItemStack> cursor) {
		if (!NBTEditorClient.CLIENT_CHEST.isLoaded()) {
			MainUtil.client.player.sendMessage(TextInst.translatable("nbteditor.client_chest.not_ready"), false);
			return;
		}
		
		ClientChestPage page = NBTEditorClient.CLIENT_CHEST.getPage(PAGE);
		if (!page.isInThisVersion()) {
			cursor.ifPresent(MainUtil::setInventoryCursorStack);
			MainUtil.client.setScreen(new ClientChestDataVersionScreen(page.getDataVersionStatus()));
			return;
		}
		
		if (MainUtil.client.currentScreen instanceof ClientChestScreen screen) {
			((ClientChestHandler) screen.handler).fillPage();
			screen.dynamicItems = NBTEditorClient.CLIENT_CHEST.getPage(PAGE).dynamicItems();
			screen.updatePageNavigation();
		} else {
			ClientChestHandler handler = new ClientChestHandler();
			handler.setCursorStack(cursor.orElse(MainUtil.client.player.playerScreenHandler.getCursorStack()));
			ClientChestScreen screen = new ClientChestScreen(handler);
			screen.dynamicItems = NBTEditorClient.CLIENT_CHEST.getPage(PAGE).dynamicItems();
			MainUtil.client.setScreen(screen);
			NBTEditorClient.CLIENT_CHEST.warnIfCorrupt();
		}
	}
	public static void show() {
		show(Optional.empty());
	}
	
	
	private static record SaveRequest(int page, ItemStack[] items, DynamicItems dynamicItems) {}
	private final SaveQueue saveQueue = new SaveQueue("Client Chest", (SaveRequest request) -> {
		try {
			NBTEditorClient.CLIENT_CHEST.setPage(request.page(), request.items(), request.dynamicItems());
		} catch (Exception e) {
			NBTEditor.LOGGER.error("Error while saving client chest", e);
			this.client.player.sendMessage(TextInst.translatable("nbteditor.client_chest.save_error"), false);
		}
	}, true);
	private boolean saved;
	
	private DynamicItems dynamicItems;
	private boolean navigationClicked;
	private NamedTextFieldWidget nameField;
	private ButtonWidget prevPage;
	private TextFieldWidget pageField;
	private ButtonWidget nextPage;
	private ButtonWidget prevPageJump;
	private ButtonWidget nextPageJump;
	private ItemStack[] prevInv;
	
	private ClientChestScreen(ClientChestHandler handler) {
		super(handler, TextInst.translatable("nbteditor.client_chest"));
		this.saved = true;
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
					try {
						NBTEditorClient.CLIENT_CHEST.setNameOfPage(PAGE, nameField.getText());
					} catch (IOException e) {
						NBTEditor.LOGGER.error("Error while saving client chest", e);
						client.player.sendMessage(TextInst.translatable("nbteditor.client_chest.save_error"), false);
					}
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
			try {
				NBTEditorClient.CLIENT_CHEST.setNameOfPage(PAGE, name);
			} catch (IOException e) {
				NBTEditor.LOGGER.error("Error while saving client chest", e);
				client.player.sendMessage(TextInst.translatable("nbteditor.client_chest.save_error"), false);
			}
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
			NBTEditorClient.CLIENT_CHEST.reloadPage(PAGE);
			show();
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
		
		return !handleKeybind(keyCode, focusedSlot,
						HandledScreenItemReferenceParent.create(
								ClientChestScreen::show, () -> handler.setCursorStack(ItemStack.EMPTY)),
						slot -> new ClientChestItemReference(PAGE, slot.getIndex()), handler.getCursorStack()) &&
				!this.nameField.keyPressed(keyCode, scanCode, modifiers) && !this.nameField.isActive() &&
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
		
		prevInv = new ItemStack[this.handler.getInventory().size()];
		for (int i = 0; i < prevInv.length; i++)
			prevInv[i] = this.handler.getInventory().getStack(i).copy();
		
		super.onMouseClick(slot, slotId, button, actionType);
	}
	@Override
	public boolean allowEnchantmentCombine(Slot slot) {
		return !ConfigScreen.isLockSlots() || slot.inventory == MainUtil.client.player.getInventory();
	}
	@Override
	public void onEnchantmentCombine(Slot slot) {
		save();
	}
	@Override
	public SlotLockType getSlotLockType() {
		return ConfigScreen.isLockSlots() ? SlotLockType.ITEMS_LOCKED : SlotLockType.UNLOCKED;
	}
	@Override
	public ItemStack[] getPrevInventory() {
		return prevInv;
	}
	@Override
	public void onChange() {
		save();
	}
	
	private void save() {
		saved = false;
		
		ItemStack[] items = new ItemStack[54];
		for (int i = 0; i < this.handler.getInventory().size(); i++)
			items[i] = this.handler.getInventory().getStack(i).copy();
		
		saveQueue.save(() -> {
			saved = true;
		}, new SaveRequest(PAGE, items, dynamicItems.copy()));
	}
	
	@Override
	protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
		for (int slot : dynamicItems.getLockedSlots())
			MVDrawableHelper.drawSlotHighlight(matrices, handler.getSlot(slot).x, handler.getSlot(slot).y, 0x60FF0000);
		super.drawForeground(matrices, mouseX, mouseY);
	}
	
	@Override
	protected Text getRenderedTitle() {
		EditableText title = TextInst.copy(this.title).append(" (" + (PAGE + 1) + ")");
		return saved ? title : title.append("*");
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
		pageField.setText((PAGE + 1) + "");
		show();
	}
	private void nextPage() {
		if (!nextPage.active)
			return;
		PAGE++;
		pageField.setText((PAGE + 1) + "");
		show();
	}
	
	private void prevPageJump() {
		if (!prevPageJump.active)
			return;
		PAGE = prevPageJumpTarget;
		pageField.setText((PAGE + 1) + "");
		show();
	}
	private void nextPageJump() {
		if (!nextPageJump.active)
			return;
		PAGE = nextPageJumpTarget;
		pageField.setText((PAGE + 1) + "");
		show();
	}
	
}
