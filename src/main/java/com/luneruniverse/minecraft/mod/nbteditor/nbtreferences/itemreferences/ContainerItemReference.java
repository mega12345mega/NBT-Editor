package com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences;

import java.util.concurrent.atomic.AtomicBoolean;

import com.luneruniverse.minecraft.mod.nbteditor.containers.ContainerIO;
import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.ContainerScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.luneruniverse.minecraft.mod.nbteditor.util.SaveQueue;

import net.minecraft.item.ItemStack;

public class ContainerItemReference implements ItemReference {
	
	private final ItemReference container;
	private final int slot;
	private final SaveQueue save;
	
	public ContainerItemReference(ItemReference container, int slot) {
		this.container = container;
		this.slot = slot;
		
		this.save = new SaveQueue("Container", (ItemStack toSave) -> {
			ItemStack containerItem = container.getItem().copy();
			ItemStack[] contents = ContainerIO.read(containerItem);
			contents[slot] = toSave;
			ContainerIO.write(containerItem, contents);
			
			// The recursive nature causes parent containers to also write items to the screen, hence the check
			if (MainUtil.client.currentScreen instanceof ContainerScreen screen && screen.getReference() == container)
				screen.getScreenHandler().getSlot(slot).setStackNoCallbacks(toSave);
			
			AtomicBoolean done = new AtomicBoolean();
			Object lock = new Object();
			container.saveItem(containerItem, () -> {
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
	
	public ItemReference getContainer() {
		return container;
	}
	public int getSlot() {
		return slot;
	}
	
	@Override
	public ItemStack getItem() {
		return ContainerIO.read(container.getItem())[slot];
	}
	
	@Override
	public void saveItem(ItemStack toSave, Runnable onFinished) {
		save.save(onFinished, toSave.copy());
	}
	
	@Override
	public boolean isLocked() {
		return container.isLocked();
	}
	
	@Override
	public boolean isLockable() {
		return container.isLockable();
	}
	
	@Override
	public int getBlockedInvSlot() {
		return container.getBlockedInvSlot();
	}
	
	@Override
	public int getBlockedHotbarSlot() {
		return container.getBlockedHotbarSlot();
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
