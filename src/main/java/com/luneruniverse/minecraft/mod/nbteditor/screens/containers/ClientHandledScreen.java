package com.luneruniverse.minecraft.mod.nbteditor.screens.containers;

import java.util.Optional;
import java.util.function.Function;

import org.lwjgl.glfw.GLFW;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import com.luneruniverse.minecraft.mod.nbteditor.commands.get.GetLostItemCommand;
import com.luneruniverse.minecraft.mod.nbteditor.containers.ContainerIO;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IdentifierInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.OldEventBehavior;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.PassContainerSlotUpdates;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.HandledScreenItemReference.HandledScreenItemReferenceParent;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.InventoryItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.NBTEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.factories.LocalFactoryScreen;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.ItemTagReferences;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.Enchants;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ClientHandledScreen extends GenericContainerScreen implements OldEventBehavior, PassContainerSlotUpdates {
	
	private static final Identifier TEXTURE = IdentifierInst.of("textures/gui/container/generic_54.png");
	
	private static Inventory SERVER_INV;
	private static ItemStack SERVER_CURSOR;
	
	private static boolean updatingServerInventory;
	private static void updateServerInventory() {
		if (!(MainUtil.client.currentScreen instanceof ClientHandledScreen)) {
			NBTEditor.LOGGER.warn("Attempted to update the server inventory when a ClientHandledScreen wasn't open!");
			return;
		}
		
		try {
			updatingServerInventory = true;
			
			ClientHandledScreen screen = (ClientHandledScreen) MainUtil.client.currentScreen;
			Inventory clientInv = MainUtil.client.player.getInventory();
			ItemStack clientCursor = screen.handler.getCursorStack();
			
			for (int i = 0; i < clientInv.size(); i++) {
				ItemStack clientStack = clientInv.getStack(i);
				if (!ItemStack.areEqual(clientStack, SERVER_INV.getStack(i))) {
					if (36 <= i && i <= 39) { // Armor
						SERVER_INV.setStack(i, clientStack.copy());
						continue;
					}
					MainUtil.clickCreativeStack(clientStack, i == 40 ? 45 : (i < 9 ? i + 36 : i));
					SERVER_INV.setStack(i, clientStack.copy());
				}
			}
			if (!ItemStack.areEqual(clientCursor, SERVER_CURSOR)) {
				// Don't update server; cursor acts like the creative inventory
				SERVER_CURSOR = clientCursor.copy();
			}
		} finally {
			updatingServerInventory = false;
		}
	}
	public static boolean isUpdatingServerInventory() {
		return updatingServerInventory;
	}
	
	public static boolean handleKeybind(int keyCode, Slot hoveredSlot, HandledScreenItemReferenceParent parent,
			Function<Slot, ItemReference> containerRef, ItemStack cursor) {
		if (hoveredSlot != null &&
				(ConfigScreen.isAirEditable() || hoveredSlot.getStack() != null && !hoveredSlot.getStack().isEmpty())) {
			int slot = hoveredSlot.getIndex();
			ItemReference ref;
			if (hoveredSlot.inventory == MainUtil.client.player.getInventory()) {
				ref = new InventoryItemReference(slot >= 36 ? slot - 36 : slot);
				if (parent != null)
					((InventoryItemReference) ref).setParent(parent);
			} else
				ref = containerRef.apply(hoveredSlot);
			return handleKeybind(keyCode, hoveredSlot.getStack(), ref, cursor);
		}
		return false;
	}
	public static boolean handleKeybind(int keyCode, ItemStack item, ItemReference ref, ItemStack cursor) {
		if (keyCode == GLFW.GLFW_KEY_DELETE) {
			if (item == null || item.isEmpty())
				return false;
			GetLostItemCommand.addToHistory(item);
			ref.saveItem(ItemStack.EMPTY);
			return true;
		}
		if (keyCode != GLFW.GLFW_KEY_SPACE)
			return false;
		
		boolean notAir = item != null && !item.isEmpty();
		if (hasControlDown()) {
			if (notAir && ContainerIO.isContainer(item))
				ContainerScreen.show(ref, Optional.of(cursor));
		} else if (hasShiftDown()) {
			if (notAir) {
				if (cursor != null && !cursor.isEmpty()) {
					MainUtil.get(cursor, true);
					ref.clearParentCursor();
				}
				MainUtil.client.setScreen(new LocalFactoryScreen<>(ref));
			}
		} else {
			if (cursor != null && !cursor.isEmpty()) {
				MainUtil.get(cursor, true);
				ref.clearParentCursor();
			}
			MainUtil.client.setScreen(new NBTEditorScreen<>(ref));
		}
		
		return true;
	}
	
	public ClientHandledScreen(GenericContainerScreenHandler handler, Text title) {
		super(handler, MainUtil.client.player.getInventory(), title);
		handler.disableSyncing();
		MainUtil.client.player.currentScreenHandler = handler;
	}
	
	@Override
	protected void init() {
		super.init();
		
		SERVER_INV = new SimpleInventory(client.player.getInventory().size());
		for (int i = 0; i < client.player.getInventory().size(); i++)
			SERVER_INV.setStack(i, client.player.getInventory().getStack(i).copy());
		SERVER_CURSOR = this.handler.getCursorStack().copy();
	}
	
	protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
		MVDrawableHelper.drawTexture(matrices, TEXTURE, x, y, 0, 0, backgroundWidth, handler.getRows() * 18 + 17);
		MVDrawableHelper.drawTexture(matrices, TEXTURE, x, y + handler.getRows() * 18 + 17, 0, 126, backgroundWidth, 96);
		
		if (showLogo())
			MainUtil.renderLogo(matrices);
	}
	@Override
	protected final void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
		drawBackground(MVDrawableHelper.getMatrices(context), delta, mouseX, mouseY);
	}
	protected final void method_2389(MatrixStack matrices, float delta, int mouseX, int mouseY) {
		drawBackground(matrices, delta, mouseX, mouseY);
	}
	protected boolean showLogo() {
		return true;
	}
	
	protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
		getLockedSlotsInfo().renderLockedHighlights(matrices, handler, true, false, true);
		
		MVDrawableHelper.drawTextWithoutShadow(matrices, textRenderer, getRenderedTitle(), titleX, titleY, 4210752);
		MVDrawableHelper.drawTextWithoutShadow(matrices, textRenderer, playerInventoryTitle, playerInventoryTitleX, playerInventoryTitleY, 4210752);
	}
	@Override
	protected final void drawForeground(DrawContext context, int mouseX, int mouseY) {
		drawForeground(MVDrawableHelper.getMatrices(context), mouseX, mouseY);
	}
	protected final void method_2388(MatrixStack matrices, int mouseX, int mouseY) {
		drawForeground(matrices, mouseX, mouseY);
	}
	protected Text getRenderedTitle() {
		return title;
	}
	
	public void setInitialFocus(Element element) {
		MVMisc.setInitialFocus(this, element, super::setInitialFocus);
	}
	@Override
	protected void setInitialFocus() {}
	
	public boolean shouldPause() {
		return false;
	}
	
	public void close() {
		MainUtil.setInventoryCursorStack(handler.getCursorStack());
		handler.setCursorStack(ItemStack.EMPTY);
		
		MainUtil.client.player.closeHandledScreen();
	}
	@Override
	public void removed() {
		// Don't always drop cursor in older versions
	}
	
	
	@Override
	protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
		if (slot != null) {
			LockedSlotsInfo lockedSlotsInfo = getLockedSlotsInfo();
			if (lockedSlotsInfo.isBlocked(slot, button, actionType, false)) {
				if (lockedSlotsInfo.isCopyLockedItem() && slot.inventory != client.player.getInventory()) {
					switch (actionType) {
						case PICKUP, PICKUP_ALL -> {
							ItemStack item = slot.getStack();
							if (item.isEmpty())
								break;
							if (!handler.getCursorStack().isEmpty() &&
									!ItemStack.areItemsAndComponentsEqual(item, handler.getCursorStack())) {
								GetLostItemCommand.loseItem(handler.getCursorStack());
								handler.setCursorStack(ItemStack.EMPTY);
							}
							ItemStack cursor = handler.getCursorStack();
							if (!cursor.isEmpty()) {
								cursor.setCount(Math.min(cursor.getMaxCount(), cursor.getCount() + item.getCount()));
								handler.setCursorStack(cursor);
							} else
								handler.setCursorStack(item.copy());
							updateServerInventory();
						}
						case CLONE -> {
							ItemStack item = slot.getStack();
							if (item.isEmpty())
								break;
							if (!handler.getCursorStack().isEmpty())
								break;
							item = item.copy();
							item.setCount(item.getMaxCount());
							handler.setCursorStack(item);
							updateServerInventory();
						}
						case QUICK_MOVE -> {
							ItemStack prevItem = slot.getStack().copy();
							LockableSlot.unlockDuring(() -> handler.onSlotClick(slot.id, button, actionType, MainUtil.client.player));
							slot.setStack(prevItem);
							updateServerInventory();
						}
						case THROW -> {
							ItemStack item = slot.getStack();
							if (button == 0) {
								item = item.copy();
								item.setCount(1);
							}
							MainUtil.dropCreativeStack(item);
						}
						case SWAP -> {}
						case QUICK_CRAFT -> throw new IllegalArgumentException("Invalid SlotActionType: " + actionType);
					}
				}
				return;
			}
		}
		
		if (!(this instanceof CursorHistoryScreen))
			GetLostItemCommand.addToHistory(handler.getCursorStack());
		
		if (!(slot != null && allowEnchantmentCombine() && Screen.hasControlDown() && tryCombineEnchantments(slot, actionType)))
			handler.onSlotClick(slot == null ? slotId : slot.id, button, actionType, MainUtil.client.player);
		
		if (!(this instanceof CursorHistoryScreen))
			GetLostItemCommand.addToHistory(handler.getCursorStack());
		
		updateServerInventory();
		onChange();
	}
	
	private boolean tryCombineEnchantments(Slot slot, SlotActionType actionType) {
		if (actionType == SlotActionType.PICKUP && slot != null) {
			ItemStack cursor = handler.getCursorStack();
			ItemStack item = slot.getStack();
			if (cursor == null || cursor.isEmpty() || item == null || item.isEmpty())
				return false;
			if (cursor.getItem() == Items.ENCHANTED_BOOK || item.getItem() == Items.ENCHANTED_BOOK) {
				if (cursor.getItem() != Items.ENCHANTED_BOOK) { // Make sure the cursor is an enchanted book
					ItemStack temp = cursor;
					cursor = item;
					item = temp;
				}
				
				Enchants enchants = ItemTagReferences.ENCHANTMENTS.get(item);
				enchants.addEnchants(ItemTagReferences.ENCHANTMENTS.get(cursor).getEnchants());
				ItemTagReferences.ENCHANTMENTS.set(item, enchants);
				
				slot.setStackNoCallbacks(item);
				handler.setCursorStack(ItemStack.EMPTY);
				return true;
			}
		}
		
		return false;
	}
	public boolean allowEnchantmentCombine() {
		return false;
	}
	
	public LockedSlotsInfo getLockedSlotsInfo() {
		return LockedSlotsInfo.NONE;
	}
	public void onChange() {
		
	}
	
}
