package com.luneruniverse.minecraft.mod.nbteditor.screens.containers;

import org.lwjgl.glfw.GLFW;

import com.luneruniverse.minecraft.mod.nbteditor.containers.ContainerIO;
import com.luneruniverse.minecraft.mod.nbteditor.itemreferences.ContainerItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVTooltip;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.factories.ItemFactoryScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

public class ContainerScreen extends ClientHandledScreen {
	
	private boolean saved;
	private final Text unsavedTitle;
	
	private ItemReference ref;
	private ItemStack item;
	private int blockedInvSlot;
	private int blockedHotbarSlot;
	private int numSlots;
	
	private ItemStack[] prevInv;
	private boolean navigationClicked;
	
	public ContainerScreen(GenericContainerScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		
		this.saved = true;
		this.unsavedTitle = TextInst.copy(title).append("*");
	}
	private ContainerScreen build(ItemReference ref) {
		this.ref = ref;
		this.item = ref.getItem().copy();
		this.blockedInvSlot = ref.getBlockedInvSlot();
		if (this.blockedInvSlot != -1)
			this.blockedInvSlot += 27;
		this.blockedHotbarSlot = ref.getBlockedHotbarSlot();
		
		ItemStack[] contents = ContainerIO.read(item);
		for (int i = 0; i < contents.length; i++)
			this.handler.getSlot(i).setStackNoCallbacks(contents[i] == null ? ItemStack.EMPTY : contents[i]);
		this.numSlots = contents.length;
		
		return this;
	}
	public static void show(ItemReference ref) {
		PlayerInventory inv = MainUtil.client.player.getInventory();
		MainUtil.client.setScreen(new ContainerScreen(new ContainerHandler(0, inv), inv, TextInst.translatable("nbteditor.container.title")
				.append(ref.getItem().getName())).build(ref));
	}
	
	@Override
	protected void init() {
		super.init();
		
		if (ref.isLockable()) {
			this.addDrawableChild(MVMisc.newButton(16, 64, 83, 20, ConfigScreen.isLockSlots() ? TextInst.translatable("nbteditor.client_chest.slots.unlock") : TextInst.translatable("nbteditor.client_chest.slots.lock"), btn -> {
				navigationClicked = true;
				if (ConfigScreen.isLockSlotsRequired()) {
					btn.active = false;
					ConfigScreen.setLockSlots(true);
				} else
					ConfigScreen.setLockSlots(!ConfigScreen.isLockSlots());
				btn.setMessage(ConfigScreen.isLockSlots() ? TextInst.translatable("nbteditor.client_chest.slots.unlock") : TextInst.translatable("nbteditor.client_chest.slots.lock"));
			})).active = !ConfigScreen.isLockSlotsRequired();
		}
		
		addDrawableChild(MVMisc.newTexturedButton(width - 36, 22, 20, 20, 20,
				ItemFactoryScreen.FACTORY_ICON,
				btn -> client.setScreen(new ItemFactoryScreen(ref)),
				new MVTooltip("nbteditor.item_factory")));
	}
	
	@Override
	protected Text getRenderedTitle() {
		return saved ? title : unsavedTitle;
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
		if (slot != null && slot.id == this.blockedInvSlot)
			return;
		if (actionType == SlotActionType.SWAP && button == blockedHotbarSlot)
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
		return slot.id != this.blockedInvSlot;
	}
	@Override
	public void onEnchantmentCombine(Slot slot) {
		save();
	}
	@Override
	public SlotLockType getSlotLockType() {
		return ref.isLocked() ? SlotLockType.ITEMS_LOCKED : SlotLockType.UNLOCKED;
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
			if (focusedSlot != null && (focusedSlot.id < numSlots || focusedSlot.inventory != this.handler.getInventory())) {
				if (handleKeybind(keyCode, focusedSlot, slot -> new ContainerItemReference(ref, slot.getIndex())))
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
