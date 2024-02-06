package com.luneruniverse.minecraft.mod.nbteditor.nbtreferences;

import java.util.Set;

import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public interface NBTReference {
	public Identifier getId();
	public Set<Identifier> getIdOptions();
	
	public NbtCompound getNBT();
	public void saveNBT(Identifier id, NbtCompound toSave, Runnable onFinished);
	public default void saveNBT(Identifier id, NbtCompound toSave, Text msg) {
		saveNBT(id, toSave, () -> MainUtil.client.player.sendMessage(msg, false));
	}
	public default void saveNBT(Identifier id, NbtCompound toSave) {
		saveNBT(id, toSave, () -> {});
	}
}
