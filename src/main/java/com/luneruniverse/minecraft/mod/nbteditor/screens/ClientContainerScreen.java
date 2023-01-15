package com.luneruniverse.minecraft.mod.nbteditor.screens;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import com.luneruniverse.minecraft.mod.nbteditor.commands.get.GetLostItemCommand;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.EnchantmentHelper;
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

public class ClientContainerScreen extends GenericContainerScreen {
	
	private static final Identifier TEXTURE = new Identifier("textures/gui/container/generic_54.png");
	
	
	
	private static Inventory SERVER_INV;
	private static ItemStack SERVER_CURSOR;
	
	public static void updateServerInventory() {
		if (!(MainUtil.client.currentScreen instanceof ClientContainerScreen)) {
			NBTEditor.LOGGER.warn("Attempted to update the server inventory when the client chest wasn't open!");
			return;
		}
		
		ClientContainerScreen screen = (ClientContainerScreen) MainUtil.client.currentScreen;
		Inventory clientInv = MainUtil.client.player.getInventory();
		ItemStack clientCursor = screen.handler.getCursorStack();
		
		for (int i = 0; i < clientInv.size(); i++) {
			ItemStack clientStack = clientInv.getStack(i);
			if (!ItemStack.areEqual(clientStack, SERVER_INV.getStack(i))) {
				if (i > 35) { // Armor
					SERVER_INV.setStack(i, clientStack);
					continue;
				}
				if (i == 40)
					i = 45;
				MainUtil.client.interactionManager.clickCreativeStack(clientStack, i < 9 ? i + 36 : i);
				if (i != 45)
					SERVER_INV.setStack(i, clientStack);
			}
		}
		if (!ItemStack.areEqual(clientCursor, SERVER_CURSOR)) {
//			client.interactionManager.clickCreativeStack(clientCursor, -1);
			SERVER_CURSOR = clientCursor;
		}
	}
	
	
	protected boolean dropCursorOnClose;
	
	public ClientContainerScreen(GenericContainerScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		this.dropCursorOnClose = true;
		handler.disableSyncing();
		MainUtil.client.player.currentScreenHandler = handler;
	}
	public static GenericContainerScreenHandler createGenericScreenHandler(int rows) {
		PlayerInventory inv = MainUtil.client.player.getInventory();
		
		switch (rows) {
			case 1:
				return GenericContainerScreenHandler.createGeneric9x1(0, inv);
			case 2:
				return GenericContainerScreenHandler.createGeneric9x2(0, inv);
			case 3:
				return GenericContainerScreenHandler.createGeneric9x3(0, inv);
			case 4:
				return GenericContainerScreenHandler.createGeneric9x4(0, inv);
			case 5:
				return GenericContainerScreenHandler.createGeneric9x5(0, inv);
			case 6:
				return GenericContainerScreenHandler.createGeneric9x6(0, inv);
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
	
	@Override
	protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
		RenderSystem.setShader(GameRenderer::getPositionTexProgram); // getPositionTexShader <= 1.19.2
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURE);
		int i = x;
		int j = y;
		this.drawTexture(matrices, i, j, 0, 0, this.backgroundWidth, this.handler.getRows() * 18 + 17);
		this.drawTexture(matrices, i, j + this.handler.getRows() * 18 + 17, 0, 126, this.backgroundWidth, 96);
	}
	
	public final boolean isPauseScreen() { // 1.18
		return shouldPause();
	}
	public boolean shouldPause() { // 1.19
		return false;
	}
	
	public final void onClose() { // 1.18
		close();
	}
	public void close() { // 1.19
		ItemStack cursor = this.handler.getCursorStack();
		if (dropCursorOnClose && cursor != null && !cursor.isEmpty())
			MainUtil.get(cursor, true);
		
		// super.close();
		client.player.closeHandledScreen();
		client.setScreen(null);
	}
	
	
	
	private ItemStack beforeClickItem;
	private ItemStack beforeClickCursor;
	
	@Override
	protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
		if (slot != null)
			beforeClickItem = slot.getStack().copy();
		beforeClickCursor = this.handler.getCursorStack().copy();
		
		if (slot != null && allowEnchantmentCombine(slot) && Screen.hasControlDown() && tryCombineEnchantments(slot, actionType))
			onEnchantmentCombine(slot);
		else
			super.onMouseClick(slot, slotId, button, actionType);
		
		ItemStack[] prev = getPrevInventory();
		if (prev == null)
			return;
		
		boolean changed = false;
		boolean lockRevertUsed = false;
		boolean locked = lockSlots();
		ItemStack[] prevInv = getPrevInventory();
		for (int i = 0; i < this.handler.getInventory().size(); i++) {
			if (!ItemStack.areEqual(prevInv[i] == null ? ItemStack.EMPTY : prevInv[i], this.handler.getInventory().getStack(i))) {
				if (locked) {
					if (prevInv[i] == null || prevInv[i].isEmpty())
						changed = true;
					else {
						this.handler.getInventory().setStack(i, prevInv[i]);
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
		
		if (lockRevertUsed && beforeClickCursor != null && !beforeClickCursor.isEmpty() && client.player.isCreative())
			GetLostItemCommand.loseItem(beforeClickCursor);
	}
	
	public void throwCursor() {
		client.interactionManager.dropCreativeStack(beforeClickCursor);
	}
	public void throwSlot(int slot, boolean fullStack) {
		ItemStack stack = beforeClickItem;
		if (!fullStack)
			stack.setCount(1);
		client.interactionManager.dropCreativeStack(stack);
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
				
				MainUtil.addEnchants(EnchantmentHelper.get(cursor), item);
				slot.setStack(item);
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
	
	
	public boolean lockSlots() {
		return false;
	}
	public ItemStack[] getPrevInventory() {
		return null; // Doesn't support slot locking or onChange calls
	}
	public void onChange() {
		
	}
	
}
