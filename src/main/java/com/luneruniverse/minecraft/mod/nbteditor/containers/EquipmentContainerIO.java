package com.luneruniverse.minecraft.mod.nbteditor.containers;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.subjectio.SubjectIOs;
import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.SlotTexture;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class EquipmentContainerIO implements ContainerIO<NbtCompound> {
	
	private static final SlotTexture[] HORSE_ARMOR_TEXTURES = new SlotTexture[] {
			SlotTexture.HELMET, SlotTexture.CHESTPLATE, SlotTexture.LEGGINGS, SlotTexture.BOOTS,
			SlotTexture.SWORD, SlotTexture.SHIELD, SlotTexture.SADDLE, SlotTexture.HORSE_ARMOR};
	private static final SlotTexture[] LLAMA_ARMOR_TEXTURES = new SlotTexture[] {
			SlotTexture.HELMET, SlotTexture.CHESTPLATE, SlotTexture.LEGGINGS, SlotTexture.BOOTS,
			SlotTexture.SWORD, SlotTexture.SHIELD, SlotTexture.SADDLE, SlotTexture.LLAMA_ARMOR};
	private static final String[] KEYS = new String[] {
			"head", "chest", "legs", "feet", "mainhand", "offhand", "saddle", "body"};
	
	private final SlotTexture[] textures;
	
	public EquipmentContainerIO(boolean llama) {
		textures = (llama ? LLAMA_ARMOR_TEXTURES : HORSE_ARMOR_TEXTURES);
	}
	
	public ContainerIO<NbtCompound> forNbtCompoundEquipment() {
		return DelegateContainerIO.map(this,
				nbt -> nbt.nbte$getCompoundOrDefault("equipment"), (nbt, list) -> nbt.put("equipment", list));
	}
	
	@Override
	public boolean isSupported(NbtCompound container) {
		return true;
	}
	
	@Override
	public int getMaxSlots(NbtCompound container) {
		return 8;
	}
	
	@Override
	public SlotTexture[] getTextures(NbtCompound container) {
		return textures;
	}
	
	@Override
	public ItemStack[] read(NbtCompound container) {
		ItemStack[] contents = new ItemStack[8];
		for (int i = 0; i < 8; i++) {
			if (container.nbte$contains(KEYS[i], NbtElement.COMPOUND_TYPE))
				contents[i] = SubjectIOs.ITEM.deserializeOrElse(container.getCompoundOrEmpty(KEYS[i]), ItemStack.EMPTY);
			else
				contents[i] = ItemStack.EMPTY;
		}
		return contents;
	}
	
	@Override
	public int write(NbtCompound container, ItemStack[] contents) {
		for (int i = 0; i < 8; i++) {
			ItemStack item = contents[i];
			if (item == null || item.isEmpty())
				container.remove(KEYS[i]);
			else
				container.put(KEYS[i], item.nbte$serialize(true));
		}
		return 8;
	}
	
	@Override
	public int getNumWritten(NbtCompound container, ItemStack[] contents) {
		return 8;
	}
	
	@Override
	public int getWrittenSlotIndex(NbtCompound container, ItemStack[] contents, int slot) {
		return slot;
	}
	
}
