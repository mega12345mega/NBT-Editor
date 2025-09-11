package com.luneruniverse.minecraft.mod.nbteditor.containers;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.manager.NBTManagers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

public class EquipmentContainerIO implements ContainerIO<NbtCompound> {
	
	private static final Identifier[] HORSE_ARMOR_TEXTURES = new Identifier[] {
			HELMET_TEXTURE, CHESTPLATE_TEXTURE, LEGGINGS_TEXTURE, BOOTS_TEXTURE,
			SADDLE_TEXTURE, HORSE_ARMOR_TEXTURE, SWORD_TEXTURE, SHIELD_TEXTURE};
	private static final Identifier[] LLAMA_ARMOR_TEXTURES = new Identifier[] {
			HELMET_TEXTURE, CHESTPLATE_TEXTURE, LEGGINGS_TEXTURE, BOOTS_TEXTURE,
			SADDLE_TEXTURE, LLAMA_ARMOR_TEXTURE, SWORD_TEXTURE, SHIELD_TEXTURE};
	private static final String[] KEYS = new String[] {
			"head", "chest", "legs", "boots", "saddle", "body", "mainhand", "offhand"};
	
	private final Identifier[] textures;
	
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
	public Identifier[] getTextures(NbtCompound container) {
		return textures;
	}
	
	@Override
	public ItemStack[] read(NbtCompound container) {
		ItemStack[] contents = new ItemStack[8];
		for (int i = 0; i < 8; i++) {
			if (container.nbte$contains(KEYS[i], NbtElement.COMPOUND_TYPE))
				contents[i] = NBTManagers.ITEM.deserializeOrElse(container.getCompoundOrEmpty(KEYS[i]), ItemStack.EMPTY);
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
