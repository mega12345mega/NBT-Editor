package com.luneruniverse.minecraft.mod.nbteditor.tagreferences;

import java.util.ArrayList;
import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalEntity;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.NBTTagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.general.TagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.AttributesNBTTagReference;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.specific.data.AttributeData;

public class EntityTagReferences {
	
	public static final TagReference<List<AttributeData>, LocalEntity> ATTRIBUTES =
			TagReference.forLocalNBT(ArrayList::new, new AttributesNBTTagReference(false));
	
	public static final TagReference<Boolean, LocalEntity> CUSTOM_NAME_VISIBLE =
			TagReference.forLocalNBT(() -> false, new NBTTagReference<>(Boolean.class, "CustomNameVisible"));
	
}
