package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.subjectio;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class BlockEntitySharedSubjectIO {
	
	private static final boolean CUSTOM_NAME_IS_SAFE = Version.<Boolean>newSwitch()
			.range("1.20.6", null, true)
			.range(null, "1.20.5", false)
			.get();
	
	public static NbtCompound stripInvalidCustomName(NbtCompound nbt) {
		if (CUSTOM_NAME_IS_SAFE || !nbt.nbte$contains("CustomName", NbtElement.STRING_TYPE))
			return nbt;
		
		String json = nbt.nbte$getStringOrDefault("CustomName");
		try {
			TextInst.fromJson(json);
		} catch (Exception e) {
			NBTEditor.LOGGER.warn("Failed to parse custom name from string '{}', discarding", json, e);
			nbt = nbt.copy();
			nbt.remove("CustomName");
		}
		
		return nbt;
	}
	
}
