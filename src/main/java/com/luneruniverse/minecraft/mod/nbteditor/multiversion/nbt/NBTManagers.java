package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.components.ComponentBlockEntityNBTManager;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.components.ComponentEntityNBTManager;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.components.ComponentItemNBTManager;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.nbt.NBTBlockEntityNBTManager;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.nbt.NBTEntityNBTManager;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.nbt.NBTItemNBTManager;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

public class NBTManagers {
	public static final boolean COMPONENTS_EXIST;
	public static final DeserializableNBTManager<ItemStack> ITEM;
	public static final NBTManager<BlockEntity> BLOCK_ENTITY;
	public static final NBTManager<Entity> ENTITY;
	
	static {
		COMPONENTS_EXIST = Version.<Boolean>newSwitch()
				.range("1.20.5", null, true)
				.range(null, "1.20.4", false)
				.get();
		if (COMPONENTS_EXIST) {
			ITEM = new ComponentItemNBTManager();
			BLOCK_ENTITY = new ComponentBlockEntityNBTManager();
			ENTITY = new ComponentEntityNBTManager();
		} else {
			ITEM = new NBTItemNBTManager();
			BLOCK_ENTITY = new NBTBlockEntityNBTManager();
			ENTITY = new NBTEntityNBTManager();
		}
	}
}
