package com.luneruniverse.minecraft.mod.nbteditor.screens;

import org.lwjgl.glfw.GLFW;

import com.luneruniverse.minecraft.mod.nbteditor.containers.ContainerIO;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.util.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

public class ItemsScreen extends ClientContainerScreen {
	
	private boolean saved;
	private final Text unsavedTitle;
	
	private ItemReference ref;
	private ItemStack item;
	private int slot;
	private int numSlots;
	
	private ItemStack[] prevInv;
	private boolean navigationClicked;
	
	public ItemsScreen(GenericContainerScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		
		this.saved = true;
		this.unsavedTitle = MultiVersionMisc.copyText(title).append("*");
	}
	private ItemsScreen build(ItemReference ref) {
		this.ref = ref;
		this.item = ref.getItem().copy();
		this.slot = ref.getHand() == Hand.MAIN_HAND ? ref.getHotbarSlot() + 36 + 27 - 9 : (ref.getHand() == Hand.OFF_HAND ? 40 : ref.getInvSlot() + 27);
		
		ItemStack[] contents = ContainerIO.read(item);
		for (int i = 0; i < contents.length; i++)
			this.handler.getSlot(i).setStack(contents[i] == null ? ItemStack.EMPTY : contents[i]);
		this.numSlots = contents.length;
		
		return this;
	}
	public static void show(ItemReference ref) {
		PlayerInventory inv = MainUtil.client.player.getInventory();
		MainUtil.client.setScreen(new ItemsScreen(new ItemsHandler(0, inv), inv, TextInst.translatable("nbteditor.container.title")
				.append(ref.getItem().getName())).build(ref));
	}
	
	@Override
	protected void init() {
		super.init();
		
		if (ref.isLockable()) {
			this.addDrawableChild(new ButtonWidget(16, 64, 83, 20, ConfigScreen.isLockSlots() ? TextInst.translatable("nbteditor.client_chest.slots.unlock") : TextInst.translatable("nbteditor.client_chest.slots.lock"), btn -> {
				navigationClicked = true;
				ConfigScreen.setLockSlots(!ConfigScreen.isLockSlots());
				btn.setMessage(ConfigScreen.isLockSlots() ? TextInst.translatable("nbteditor.client_chest.slots.unlock") : TextInst.translatable("nbteditor.client_chest.slots.lock"));
			})).active = !ConfigScreen.isLockSlotsRequired();
		}
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);
		MainUtil.renderLogo(matrices);
	}
	
	@Override
	protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
		this.textRenderer.draw(matrices, saved ? this.title : this.unsavedTitle, (float)this.titleX, (float)this.titleY, 4210752);
		this.textRenderer.draw(matrices, this.playerInventoryTitle, (float)this.playerInventoryTitleX, (float)this.playerInventoryTitleY, 4210752);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		navigationClicked = false;
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
		if (navigationClicked)
			return;
		if (slot != null && slot.id == this.slot)
			return;
		if (actionType == SlotActionType.SWAP && button == ref.getHotbarSlot())
			return;
		if (slot != null && slot.id >= numSlots && slot.inventory == this.handler.getInventory() && (slot.getStack() == null || slot.getStack().isEmpty()))
			return;
		
		prevInv = new ItemStack[this.handler.getInventory().size()];
		for (int i = 0; i < prevInv.length; i++)
			prevInv[i] = this.handler.getInventory().getStack(i).copy();
		
		super.onMouseClick(slot, slotId, button, actionType);
	}
	@Override
	public boolean allowEnchantmentCombine(Slot slot) {
		return slot.id != this.slot;
	}
	@Override
	public void onEnchantmentCombine(Slot slot) {
		save();
	}
	@Override
	public boolean lockSlots() {
		return ref.isLocked();
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
		ItemStack[] contents = new ItemStack[this.handler.getInventory().size()];
		for (int i = 0; i < contents.length; i++)
			contents[i] = this.handler.getInventory().getStack(i);
		ContainerIO.write(item, contents);
		
		saved = false;
		ref.saveItem(item, () -> {
			saved = true;
		});
	}
	
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (ref.keyPressed(keyCode, scanCode, modifiers))
			return true;
		
		if (keyCode == GLFW.GLFW_KEY_SPACE) {
			Slot hoveredSlot = this.focusedSlot;
			if (hoveredSlot != null && (hoveredSlot.id < numSlots || hoveredSlot.inventory != this.handler.getInventory()) && (ConfigScreen.isAirEditable() || hoveredSlot.getStack() != null && !hoveredSlot.getStack().isEmpty())) {
				int slot = hoveredSlot.getIndex();
				ItemReference ref = hoveredSlot.inventory == client.player.getInventory() ? new ItemReference(slot >= 36 ? slot - 36 : slot) : new ItemReference(this.ref, hoveredSlot.getIndex());
				if (hasControlDown()) {
					if (hoveredSlot.getStack() != null && ContainerIO.isContainer(hoveredSlot.getStack()))
						ItemsScreen.show(ref);
				} else
					client.setScreen(new NBTEditorScreen(ref));
				return true;
			}
		}
		
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	@Override
	public boolean shouldPause() {
		return true;
	}
	
	public ItemReference getReference() {
		return ref;
	}
	
	@Override
	public void removed() {
		for (int i = numSlots; i < 27; i++) { // Items that may get deleted
			ItemStack item = this.handler.getInventory().getStack(i);
			if (item != null && !item.isEmpty())
				MainUtil.get(item, true);
		}
		
		super.removed();
	}
	
}
