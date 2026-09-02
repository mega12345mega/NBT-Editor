package com.luneruniverse.minecraft.mod.nbteditor.containers;

import java.util.Arrays;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.MVNbtCompoundParent;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.subjectio.SubjectIOs;
import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.SlotTexture;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

public class SlotKeyNbtListContainerIO implements ContainerIO<NbtList> {
	
	private final int numSlots;
	private final SlotTexture[] textures;
	
	public SlotKeyNbtListContainerIO(int numSlots) {
		this.numSlots = numSlots;
		this.textures = new SlotTexture[numSlots];
	}
	
	public ContainerIO<NbtCompound> forNbtCompound(String key) {
		return DelegateContainerIO.map(this, nbt -> nbt.nbte$getListOrDefault(key), (nbt, list) -> nbt.put(key, list));
	}
	public ContainerIO<NbtCompound> forNbtCompoundItems() {
		return forNbtCompound("Items");
	}
	
	@Override
	public boolean isSupported(NbtList container) {
		for (NbtElement itemNbtElement : container) {
			if (itemNbtElement instanceof NbtCompound itemNbt) {
				if (!itemNbt.nbte$contains("Slot", MVNbtCompoundParent.NUMBER_TYPE))
					return false;
				int slot = itemNbt.nbte$getIntOrDefault("Slot");
				if (slot < 0 || slot >= numSlots)
					return false;
			} else {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public int getMaxSlots(NbtList container) {
		return numSlots;
	}
	
	@Override
	public SlotTexture[] getTextures(NbtList container) {
		return textures;
	}
	
	@Override
	public ItemStack[] read(NbtList container) {
		ItemStack[] contents = new ItemStack[numSlots];
		Arrays.fill(contents, ItemStack.EMPTY);
		for (NbtElement itemNbtElement : container) {
			NbtCompound itemNbt = (NbtCompound) itemNbtElement;
			contents[itemNbt.nbte$getIntOrDefault("Slot")] = SubjectIOs.ITEM.deserializeOrElse(itemNbt, ItemStack.EMPTY);
		}
		return contents;
	}
	
	@Override
	public int write(NbtList container, ItemStack[] contents) {
		container.clear();
		for (int i = 0; i < contents.length; i++) {
			ItemStack item = contents[i];
			if (item == null || item.isEmpty())
				continue;
			NbtCompound itemNbt = item.nbte$serialize(true);
			itemNbt.putByte("Slot", (byte) i);
			container.add(itemNbt);
		}
		return numSlots;
	}
	
	@Override
	public int getNumWritten(NbtList container, ItemStack[] contents) {
		return numSlots;
	}
	
	@Override
	public int getWrittenSlotIndex(NbtList container, ItemStack[] contents, int slot) {
		return slot;
	}
	
}
