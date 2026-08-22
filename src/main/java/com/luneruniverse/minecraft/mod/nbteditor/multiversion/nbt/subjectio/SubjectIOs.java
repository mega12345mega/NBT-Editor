package com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.subjectio;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.subjectio.component.BlockEntityComponentSubjectIO;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.subjectio.component.EntityComponentSubjectIO;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.subjectio.component.ItemComponentSubjectIO;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.subjectio.nbt.BlockEntityNBTSubjectIO;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.subjectio.nbt.EntityNBTSubjectIO;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.subjectio.nbt.ItemNBTSubjectIO;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

public class SubjectIOs {
	public static final boolean COMPONENTS_EXIST;
	public static final DeserializableSubjectIO<ItemStack> ITEM;
	public static final SubjectIO<BlockEntity> BLOCK_ENTITY;
	public static final SubjectIO<Entity> ENTITY;
	
	static {
		COMPONENTS_EXIST = Version.<Boolean>newSwitch()
				.range("1.20.5", null, true)
				.range(null, "1.20.4", false)
				.get();
		if (COMPONENTS_EXIST) {
			ITEM = new ItemComponentSubjectIO();
			BLOCK_ENTITY = new BlockEntityComponentSubjectIO();
			ENTITY = new EntityComponentSubjectIO();
		} else {
			ITEM = new ItemNBTSubjectIO();
			BLOCK_ENTITY = new BlockEntityNBTSubjectIO();
			ENTITY = new EntityNBTSubjectIO();
		}
	}
}
