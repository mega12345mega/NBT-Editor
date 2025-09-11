package com.luneruniverse.minecraft.mod.nbteditor.containers;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.manager.NBTManagers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;

public class ArmorHandsContainerIO implements ContainerIO<NbtCompound> {
	
	private static final Identifier[] TEXTURES = new Identifier[] {
			HELMET_TEXTURE, CHESTPLATE_TEXTURE, LEGGINGS_TEXTURE, BOOTS_TEXTURE, SWORD_TEXTURE, SHIELD_TEXTURE};
	
	@Override
	public boolean isSupported(NbtCompound container) {
		NbtElement armorItemsNbtElement = container.get("ArmorItems");
		if (armorItemsNbtElement != null) {
			if (!(armorItemsNbtElement instanceof NbtList armorItemsNbt) ||
					armorItemsNbt.size() > 4 ||
					armorItemsNbt.nbte$getHeldType().filter(
							heldType -> heldType == 0 || heldType == NbtElement.COMPOUND_TYPE).isEmpty()) {
				return false;
			}
		}
		
		NbtElement handItemsNbtElement = container.get("HandItems");
		if (handItemsNbtElement != null) {
			if (!(handItemsNbtElement instanceof NbtList handItemsNbt) ||
					handItemsNbt.size() > 2 ||
					handItemsNbt.nbte$getHeldType().filter(
							heldType -> heldType == 0 || heldType == NbtElement.COMPOUND_TYPE).isEmpty()) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public int getMaxSlots(NbtCompound container) {
		return 6;
	}
	
	@Override
	public Identifier[] getTextures(NbtCompound container) {
		return TEXTURES;
	}
	
	@Override
	public ItemStack[] read(NbtCompound container) {
		ItemStack[] items = new ItemStack[6];
		
		NbtList armorItemsNbt = container.nbte$getListOrDefault("ArmorItems", NbtElement.COMPOUND_TYPE);
		for (int i = 0; i < armorItemsNbt.nbte$size() && i < 4; i++)
			items[3 - i] = NBTManagers.ITEM.deserializeOrElse((NbtCompound) armorItemsNbt.nbte$get(i), ItemStack.EMPTY);
		
		NbtList handItemsNbt = container.nbte$getListOrDefault("HandItems", NbtElement.COMPOUND_TYPE);
		for (int i = 0; i < handItemsNbt.nbte$size() && i < 2; i++)
			items[4 + i] = NBTManagers.ITEM.deserializeOrElse((NbtCompound) handItemsNbt.nbte$get(i), ItemStack.EMPTY);
		
		return items;
	}
	
	@Override
	public int write(NbtCompound container, ItemStack[] contents) {
		ItemStack[] actualContents = new ItemStack[6];
		for (int i = 0; i < 6; i++) {
			ItemStack item = null;
			if (i < contents.length)
				item = contents[i];
			if (item == null)
				item = ItemStack.EMPTY;
			actualContents[i] = item;
		}
		
		NbtList armorItemsNbt = new NbtList();
		for (int i = 0; i < 4; i++)
			armorItemsNbt.add(actualContents[3 - i].nbte$serialize(true));
		container.put("ArmorItems", armorItemsNbt);
		
		NbtList handItemsNbt = new NbtList();
		for (int i = 0; i < 2; i++)
			handItemsNbt.add(actualContents[4 + i].nbte$serialize(true));
		container.put("HandItems", handItemsNbt);
		
		return 6;
	}
	
	@Override
	public int getNumWritten(NbtCompound container, ItemStack[] contents) {
		return 6;
	}
	
	@Override
	public int getWrittenSlotIndex(NbtCompound container, ItemStack[] contents, int slot) {
		return slot;
	}
	
}
