package com.luneruniverse.minecraft.mod.nbteditor.containers;

import java.util.Arrays;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.elementio.MVL;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.subjectio.SubjectIOs;
import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.SlotTexture;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

public class OrderNbtListContainerIO implements ContainerIO<NbtList> {
	
	private final int maxSlots;
	private final SlotTexture[] textures;
	
	public OrderNbtListContainerIO(int maxSlots) {
		this.maxSlots = maxSlots;
		this.textures = new SlotTexture[maxSlots];
	}
	
	public ContainerIO<NbtCompound> forNbtCompound(String key) {
		return DelegateContainerIO.map(this, nbt -> nbt.nbte$getListOrDefault(key), (nbt, list) -> nbt.put(key, list));
	}
	public ContainerIO<NbtCompound> forNbtCompoundItems() {
		return forNbtCompound("Items");
	}
	
	@Override
	public boolean isSupported(NbtList container) {
		return container.size() <= maxSlots && MVL.getHeldType(container).filter(
				heldType -> heldType == 0 || heldType == NbtElement.COMPOUND_TYPE).isPresent();
	}
	
	@Override
	public int getMaxSlots(NbtList container) {
		return maxSlots;
	}
	
	@Override
	public SlotTexture[] getTextures(NbtList container) {
		return textures;
	}
	
	@Override
	public ItemStack[] read(NbtList container) {
		return container.stream().map(itemNbt -> SubjectIOs.ITEM.deserializeOrElse(
				(NbtCompound) itemNbt, ItemStack.EMPTY)).toArray(ItemStack[]::new);
	}
	
	@Override
	public int write(NbtList container, ItemStack[] contents) {
		container.clear();
		Arrays.stream(contents).filter(item -> item != null && !item.isEmpty())
				.map(item -> item.nbte$serialize(true)).forEach(container::add);
		return contents.length;
	}
	
	@Override
	public int getNumWritten(NbtList container, ItemStack[] contents) {
		return contents.length;
	}
	
	@Override
	public int getWrittenSlotIndex(NbtList container, ItemStack[] contents, int slot) {
		int output = slot;
		for (int i = 0; i < slot; i++) {
			if (contents[i] == null || contents[i].isEmpty())
				output--;
		}
		return output;
	}
	
}
