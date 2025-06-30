package com.luneruniverse.minecraft.mod.nbteditor.tagreferences;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.NBTManagers;

public class TagNames {
	
	public static final String BLOCK_STATE_TAG;
	public static final String BLOCK_ENTITY_TAG;
	public static final String ENTITY_TAG;
	public static final String BUNDLE_CONTENTS;
	public static final String ARMOR_ITEM;
	public static final String DECOR_ITEM;
	
	static {
		if (NBTManagers.COMPONENTS_EXIST) {
			BLOCK_STATE_TAG = "minecraft:block_state";
			BLOCK_ENTITY_TAG = "minecraft:block_entity_data";
			ENTITY_TAG = "minecraft:entity_data";
			BUNDLE_CONTENTS = "minecraft:bundle_contents";
			ARMOR_ITEM = "body_armor_item";
			DECOR_ITEM = "body_armor_item";
		} else {
			BLOCK_STATE_TAG = "BlockStateTag";
			BLOCK_ENTITY_TAG = "BlockEntityTag";
			ENTITY_TAG = "EntityTag";
			BUNDLE_CONTENTS = "Items";
			ARMOR_ITEM = "ArmorItem";
			DECOR_ITEM = "DecorItem";
		}
	}
	
}
