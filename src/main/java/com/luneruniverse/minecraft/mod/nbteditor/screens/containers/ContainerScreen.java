package com.luneruniverse.minecraft.mod.nbteditor.screens.containers;

import org.lwjgl.glfw.GLFW;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.containers.ContainerIO;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalNBT;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVTooltip;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.NBTReference;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ContainerItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.factories.LocalFactoryScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

public class ContainerScreen<L extends LocalNBT> extends ClientHandledScreen {
	
	public static <L extends LocalNBT> void show(NBTReference<L> ref) {
		if (!ref.exists() || !ContainerIO.isContainer(ref.getLocalNBT())) {
			ref.showParent();
			return;
		}
		
		NBTEditorClient.CURSOR_MANAGER.showBranch(new ContainerScreen<>(ref));
	}
	
	private final Text unsavedTitle;
	
	private final NBTReference<L> ref;
	private final L localNBT;
	private final int numSlots;
	private boolean saved;
	
	private boolean navigationClicked;
	
	private ContainerScreen(NBTReference<L> ref) {
		super(new ClientScreenHandler(3), TextInst.translatable("nbteditor.container.title").append(ref.getLocalNBT().getName()));
		
		this.unsavedTitle = TextInst.copy(title).append("*");
		
		this.ref = ref;
		this.localNBT = LocalNBT.copy(ref.getLocalNBT());
		this.numSlots = ContainerIO.getMaxSize(localNBT);
		this.saved = true;
		
		ItemStack[] contents = ContainerIO.read(localNBT);
		for (int i = 0; i < contents.length; i++)
			handler.getSlot(i).setStackNoCallbacks(contents[i] == null ? ItemStack.EMPTY : contents[i]);
	}
	
	@Override
	protected void init() {
		super.init();
		
		if (ref instanceof ItemReference item && item.isLockable()) {
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
				LocalFactoryScreen.FACTORY_ICON,
				btn -> client.setScreen(new LocalFactoryScreen<>(ref)),
				new MVTooltip("nbteditor.factory")));
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
		
		super.onMouseClick(slot, slotId, button, actionType);
	}
	@Override
	public boolean allowEnchantmentCombine() {
		return true;
	}
	@Override
	public LockedSlotsInfo getLockedSlotsInfo() {
		LockedSlotsInfo info = (ref instanceof ItemReference itemRef && itemRef.isLocked()
				? LockedSlotsInfo.ITEMS_LOCKED : LockedSlotsInfo.NONE).copy();
		if (ref instanceof ItemReference itemRef)
			info.addPlayerSlot(itemRef);
		
		for (int slot = numSlots; slot < 27; slot++)
			info.addContainerSlot(slot);
		
		return info;
	}
	@Override
	public void onChange() {
		save();
	}
	private void save() {
		ItemStack[] contents = new ItemStack[this.handler.getInventory().size()];
		for (int i = 0; i < contents.length; i++)
			contents[i] = this.handler.getInventory().getStack(i);
		ContainerIO.write(localNBT, contents);
		
		saved = false;
		ref.saveLocalNBT(localNBT, () -> {
			saved = true;
		});
	}
	
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (MainUtil.client.options.inventoryKey.matchesKey(keyCode, scanCode)) {
			ref.showParent();
			return true;
		}
		
		if (focusedSlot != null && (focusedSlot.id < numSlots || focusedSlot.inventory != this.handler.getInventory())) {
			if (keyCode != GLFW.GLFW_KEY_DELETE || !getLockedSlotsInfo().isBlocked(focusedSlot, true)) {
				if (handleKeybind(keyCode, focusedSlot, () -> show(ref), slot -> getContainerRef(slot.getIndex())))
					return true;
			}
		}
		
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	private ContainerItemReference<L> getContainerRef(int slot) {
		ItemStack[] contents = new ItemStack[this.handler.getInventory().size()];
		for (int i = 0; i < contents.length; i++)
			contents[i] = this.handler.getInventory().getStack(i);
		return new ContainerItemReference<>(ref, ContainerIO.getWrittenSlotIndex(localNBT, contents, slot));
	}
	
	@Override
	protected void handledScreenTick() {
		if (!ref.exists())
			ref.showParent();
	}
	
	@Override
	public boolean shouldPause() {
		return true;
	}
	
	public NBTReference<L> getReference() {
		return ref;
	}
	
	@Override
	public void close() {
		ref.escapeParent();
	}
	@Override
	public void removed() {
		for (int i = numSlots; i < 27; i++) { // Items that will get deleted
			ItemStack item = this.handler.getInventory().getStack(i);
			if (item != null && !item.isEmpty())
				MainUtil.get(item, true);
		}
	}
	
}
