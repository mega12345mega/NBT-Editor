package com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences;

import java.util.concurrent.atomic.AtomicBoolean;

import com.luneruniverse.minecraft.mod.nbteditor.containers.ContainerIO;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalNBT;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.NBTReference;
import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.ContainerScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.luneruniverse.minecraft.mod.nbteditor.util.SaveQueue;

import net.minecraft.item.ItemStack;

public class ContainerItemReference<L extends LocalNBT> implements ItemReference {
	
	private final NBTReference<L> container;
	private final int slot;
	private final SaveQueue<ItemStack> save;
	
	public ContainerItemReference(NBTReference<L> container, int slot) {
		this.container = container;
		this.slot = slot;
		
		this.save = new SaveQueue<>("Container", toSave -> {
			L containerValue = LocalNBT.copy(container.getLocalNBT());
			ItemStack[] contents = ContainerIO.read(containerValue);
			contents[slot] = toSave;
			ContainerIO.write(containerValue, contents);
			
			if (MainUtil.client.currentScreen instanceof ContainerScreen screen && screen.getReference() == container)
				screen.getScreenHandler().getSlot(slot).setStackNoCallbacks(toSave);
			
			AtomicBoolean done = new AtomicBoolean();
			Object lock = new Object();
			container.saveLocalNBT(containerValue, () -> {
				done.set(true);
				synchronized (lock) {
					lock.notify();
				}
			});
			synchronized (lock) {
				while (!done.get()) {
					try {
						lock.wait();
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			}
		}, true);
	}
	
	public NBTReference<L> getContainer() {
		return container;
	}
	public int getSlot() {
		return slot;
	}
	
	@Override
	public boolean exists() {
		if (!container.exists())
			return false;
		
		L containerValue = container.getLocalNBT();
		return ContainerIO.isContainer(containerValue) && slot < ContainerIO.getMaxSize(containerValue);
	}
	
	@Override
	public ItemStack getItem() {
		L containerValue = container.getLocalNBT();
		ItemStack[] contents = ContainerIO.read(containerValue);
		if (slot >= contents.length && slot < ContainerIO.getMaxSize(containerValue))
			return ItemStack.EMPTY;
		return contents[slot];
	}
	
	@Override
	public void saveItem(ItemStack toSave, Runnable onFinished) {
		save.save(onFinished, toSave.copy());
	}
	
	@Override
	public boolean isLocked() {
		return container instanceof ItemReference item && item.isLocked();
	}
	
	@Override
	public boolean isLockable() {
		return container instanceof ItemReference item && item.isLockable();
	}
	
	@Override
	public int getBlockedInvSlot() {
		return container instanceof ItemReference item ? item.getBlockedInvSlot() : -1;
	}
	
	@Override
	public int getBlockedHotbarSlot() {
		return container instanceof ItemReference item ? item.getBlockedHotbarSlot() : -1;
	}
	
	@Override
	public void showParent() {
		ContainerScreen.show(container);
	}
	
	@Override
	public void escapeParent() {
		container.escapeParent();
	}
	
}
