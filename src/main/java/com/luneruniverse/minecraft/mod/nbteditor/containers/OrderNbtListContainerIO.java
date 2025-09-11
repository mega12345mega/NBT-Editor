package com.luneruniverse.minecraft.mod.nbteditor.containers;

import java.util.Arrays;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.manager.NBTManagers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;

public class OrderNbtListContainerIO implements ContainerIO<NbtList> {
	
	private final int maxSlots;
	private final Identifier[] textures;
	
	public OrderNbtListContainerIO(int maxSlots) {
		this.maxSlots = maxSlots;
		this.textures = new Identifier[maxSlots];
	}
	
	public ContainerIO<NbtCompound> forNbtCompound(String key) {
		return DelegateContainerIO.map(this, nbt -> nbt.nbte$getListOrDefault(key), (nbt, list) -> nbt.put(key, list));
	}
	public ContainerIO<NbtCompound> forNbtCompoundItems() {
		return forNbtCompound("Items");
	}
	
	@Override
	public boolean isSupported(NbtList container) {
		return container.size() <= maxSlots && container.nbte$getHeldType().filter(
				heldType -> heldType == 0 || heldType == NbtElement.COMPOUND_TYPE).isPresent();
	}
	
	@Override
	public int getMaxSlots(NbtList container) {
		return maxSlots;
	}
	
	@Override
	public Identifier[] getTextures(NbtList container) {
		return textures;
	}
	
	@Override
	public ItemStack[] read(NbtList container) {
		return container.nbte$stream().map(itemNbt -> NBTManagers.ITEM.deserializeOrElse(
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
