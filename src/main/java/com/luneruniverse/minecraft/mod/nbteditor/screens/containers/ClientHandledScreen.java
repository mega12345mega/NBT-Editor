package com.luneruniverse.minecraft.mod.nbteditor.screens.containers;

import java.util.function.Function;

import org.lwjgl.glfw.GLFW;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.commands.get.GetLostItemCommand;
import com.luneruniverse.minecraft.mod.nbteditor.containers.ContainerIO;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IdentifierInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IgnoreCloseScreenPacket;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.OldEventBehavior;
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
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ClientHandledScreen extends GenericContainerScreen implements OldEventBehavior, IgnoreCloseScreenPacket {
	
	private static final Identifier TEXTURE = IdentifierInst.of("textures/gui/container/generic_54.png");
	
	public static boolean handleKeybind(int keyCode, Slot hoveredSlot, Runnable parent, Function<Slot, ItemReference> containerRef) {
		if (hoveredSlot != null &&
				(ConfigScreen.isAirEditable() || hoveredSlot.getStack() != null && !hoveredSlot.getStack().isEmpty())) {
			ItemReference ref;
			if (hoveredSlot.inventory == MainUtil.client.player.getInventory()) {
				ref = new InventoryItemReference(hoveredSlot.getIndex());
				if (parent != null)
					((InventoryItemReference) ref).setParent(parent);
			} else
				ref = containerRef.apply(hoveredSlot);
			return handleKeybind(keyCode, hoveredSlot.getStack(), ref);
		}
		return false;
	}
	public static boolean handleKeybind(int keyCode, ItemStack item, ItemReference ref) {
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
				ContainerScreen.show(ref);
		} else if (hasShiftDown()) {
			if (notAir)
				MainUtil.client.setScreen(new LocalFactoryScreen<>(ref));
		} else
			MainUtil.client.setScreen(new NBTEditorScreen<>(ref));
		
		return true;
	}
	
	private ServerInventoryManager serverInv;
	
	protected ClientHandledScreen(GenericContainerScreenHandler handler, Text title) {
		super(handler, MainUtil.client.player.getInventory(), title);
		handler.disableSyncing();
	}
	
	public ServerInventoryManager getServerInventoryManager() {
		return serverInv;
	}
	
	@Override
	protected void init() {
		super.init();
		serverInv = new ServerInventoryManager();
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
		NBTEditorClient.CURSOR_MANAGER.closeRoot();
	}
	@Override
	public void removed() {
		serverInv = null;
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
							serverInv.updateServer();
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
							serverInv.updateServer();
						}
						case QUICK_MOVE -> {
							ItemStack prevItem = slot.getStack().copy();
							LockableSlot.unlockDuring(() -> handler.onSlotClick(slot.id, button, actionType, MainUtil.client.player));
							slot.setStack(prevItem);
							serverInv.updateServer();
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
		
		serverInv.updateServer();
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
