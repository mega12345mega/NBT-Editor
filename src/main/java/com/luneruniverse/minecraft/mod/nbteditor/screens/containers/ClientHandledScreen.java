package com.luneruniverse.minecraft.mod.nbteditor.screens.containers;

import java.util.Optional;
import java.util.function.Function;

import org.lwjgl.glfw.GLFW;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import com.luneruniverse.minecraft.mod.nbteditor.commands.get.GetLostItemCommand;
import com.luneruniverse.minecraft.mod.nbteditor.containers.ContainerIO;
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
import net.minecraft.entity.player.PlayerInventory;
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
	
	public static final int SYNC_ID = -2718;
	
	private static final Identifier TEXTURE = new Identifier("textures/gui/container/generic_54.png");
	
	private static Inventory SERVER_INV;
	private static ItemStack SERVER_CURSOR;
	
	public static void updateServerInventory() {
		if (!(MainUtil.client.currentScreen instanceof ClientHandledScreen)) {
			NBTEditor.LOGGER.warn("Attempted to update the server inventory when a ClientHandledScreen wasn't open!");
			return;
		}
		
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
	}
	
	public static boolean handleKeybind(int keyCode, Slot hoveredSlot, HandledScreenItemReferenceParent parent,
			Function<Slot, ItemReference> containerRef, ItemStack cursor) {
		if (keyCode == GLFW.GLFW_KEY_SPACE) {
			if (hoveredSlot != null && (ConfigScreen.isAirEditable() || hoveredSlot.getStack() != null && !hoveredSlot.getStack().isEmpty())) {
				int slot = hoveredSlot.getIndex();
				ItemReference ref;
				if (hoveredSlot.inventory == MainUtil.client.player.getInventory()) {
					ref = new InventoryItemReference(slot >= 36 ? slot - 36 : slot);
					if (parent != null)
						((InventoryItemReference) ref).setParent(parent);
				} else
					ref = containerRef.apply(hoveredSlot);
				handleKeybind(hoveredSlot.getStack(), ref, cursor);
				return true;
			}
		}
		return false;
	}
	public static void handleKeybind(ItemStack item, ItemReference ref, ItemStack cursor) {
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
	}
	
	
	public ClientHandledScreen(GenericContainerScreenHandler handler, Text title) {
		super(handler, MainUtil.client.player.getInventory(), title);
		handler.disableSyncing();
		MainUtil.client.player.currentScreenHandler = handler;
	}
	public static GenericContainerScreenHandler createGenericScreenHandler(int rows) {
		PlayerInventory inv = MainUtil.client.player.getInventory();
		
		switch (rows) {
			case 1:
				return GenericContainerScreenHandler.createGeneric9x1(SYNC_ID, inv);
			case 2:
				return GenericContainerScreenHandler.createGeneric9x2(SYNC_ID, inv);
			case 3:
				return GenericContainerScreenHandler.createGeneric9x3(SYNC_ID, inv);
			case 4:
				return GenericContainerScreenHandler.createGeneric9x4(SYNC_ID, inv);
			case 5:
				return GenericContainerScreenHandler.createGeneric9x5(SYNC_ID, inv);
			case 6:
				return GenericContainerScreenHandler.createGeneric9x6(SYNC_ID, inv);
			default:
				throw new IllegalArgumentException("Rows are limited to 1 to 6!");
		}
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
	
	
	
	private ItemStack beforeClickItem;
	private ItemStack beforeClickCursor;
	
	@Override
	protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
		SlotLockType lockType = getSlotLockType();
		
		if (slot != null) {
			beforeClickItem = slot.getStack().copy();
			if (slot.inventory != client.player.getInventory() && beforeClickItem.isEmpty() && lockType == SlotLockType.ALL_LOCKED)
				return;
		}
		beforeClickCursor = this.handler.getCursorStack().copy();
		
		if (!beforeClickCursor.isEmpty() && !(this instanceof CursorHistoryScreen))
			GetLostItemCommand.addToHistory(beforeClickCursor);
		
		if (slot != null && allowEnchantmentCombine(slot) && Screen.hasControlDown() && tryCombineEnchantments(slot, actionType))
			onEnchantmentCombine(slot);
		else
			super.onMouseClick(slot, slotId, button, actionType);
		
		ItemStack afterClickCursor = this.handler.getCursorStack();
		if (!afterClickCursor.isEmpty() && !(this instanceof CursorHistoryScreen))
			GetLostItemCommand.addToHistory(afterClickCursor);
		
		ItemStack[] prev = getPrevInventory();
		if (prev == null)
			return;
		
		boolean changed = false;
		boolean lockRevertUsed = false;
		for (int i = 0; i < this.handler.getInventory().size(); i++) {
			if (!ItemStack.areEqual(prev[i] == null ? ItemStack.EMPTY : prev[i], this.handler.getInventory().getStack(i))) {
				if (lockType != SlotLockType.UNLOCKED) {
					if (prev[i] == null || prev[i].isEmpty())
						changed = true;
					else {
						this.handler.getInventory().setStack(i, prev[i].copy());
						lockRevertUsed = true;
					}
				} else {
					changed = true;
					break;
				}
			}
		}
		if (changed)
			onChange();
		
		if (lockRevertUsed && beforeClickCursor != null && !beforeClickCursor.isEmpty())
			GetLostItemCommand.loseItem(beforeClickCursor);
	}
	
	public void throwCursor() {
		MainUtil.dropCreativeStack(beforeClickCursor);
	}
	public void throwSlot(int slot, boolean fullStack) {
		ItemStack stack = beforeClickItem;
		if (!fullStack)
			stack.setCount(1);
		MainUtil.dropCreativeStack(stack);
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
	public boolean allowEnchantmentCombine(Slot slot) {
		return false;
	}
	public void onEnchantmentCombine(Slot slot) {
		
	}
	
	
	public enum SlotLockType {
		UNLOCKED,
		ITEMS_LOCKED,
		ALL_LOCKED
	}
	public SlotLockType getSlotLockType() {
		return SlotLockType.UNLOCKED;
	}
	public ItemStack[] getPrevInventory() {
		return null; // Doesn't support slot locking or onChange calls
	}
	public void onChange() {
		
	}
	
}
